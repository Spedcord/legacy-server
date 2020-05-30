package xyz.spedcord.server.joinlink;

import xyz.spedcord.common.sql.MySqlService;
import xyz.spedcord.server.util.MySqlUtil;
import xyz.spedcord.server.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class JoinLinkController {

    private final MySqlService mySqlService;

    private final Set<JoinLink> joinLinks = new HashSet<>();

    public JoinLinkController(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
        init();
    }

    private void init() {
        try {
            mySqlService.update("CREATE TABLE IF NOT EXISTS joinlinks (id BIGINT AUTO_INCREMENT, stringId VARCHAR(128), " +
                    "companyId BIGINT, uses INT, maxUses INT, createdAt BIGINT, PRIMARY KEY (id))");
            load();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void load() throws SQLException {
        ResultSet resultSet = mySqlService.execute("SELECT * FROM joinlinks");
        while (resultSet.next()) {
            JoinLink joinLink = new JoinLink(
                    resultSet.getString("stringId"),
                    resultSet.getInt("companyId"),
                    resultSet.getInt("maxUses"),
                    resultSet.getInt("uses"),
                    resultSet.getLong("createdAt")
            );
            if (System.currentTimeMillis() - joinLink.getCreatedAt() >= TimeUnit.HOURS.toMillis(1)) {
                removeJoinLink(joinLink.getId());
                continue;
            }
            joinLinks.add(joinLink);
        }
    }

    public int getCompanyId(String joinId) {
        return joinLinks.stream()
                .filter(joinLink -> joinLink.getId().equals(joinId))
                .map(JoinLink::getCompanyId)
                .findAny().orElse(-1);
    }

    public void joinLinkUsed(String id) {
        Optional<JoinLink> optional = joinLinks.stream()
                .filter(joinLink -> joinLink.getId().equals(id))
                .findAny();
        if (optional.isEmpty()) {
            return;
        }

        JoinLink joinLink = optional.get();
        joinLink.setUses(joinLink.getUses() + 1);

        if (joinLink.getUses() >= joinLink.getMaxUses()) {
            removeJoinLink(id);
            return;
        }

        try {
            mySqlService.update(String.format("UPDATE joinlinks SET uses = %d WHERE stringId = '%s'",
                    joinLink.getUses(), joinLink.getId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeJoinLink(String id) {
        new HashSet<>(joinLinks).stream()
                .filter(joinLink -> joinLink.getId().equals(id))
                .findAny()
                .ifPresent(joinLinks::remove);
        try {
            mySqlService.update(String.format("DELETE FROM joinlinks WHERE stringId = '%s'", MySqlUtil.escapeString(id)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String generateNewLink(int companyId, int maxUses) {
        String str = StringUtil.generateKey(12);
        try {
            mySqlService.update(String.format("INSERT INTO joinlinks (stringId, companyId, uses, maxUses, createdAt) " +
                    "VALUES('%s', %d, 0, %d, %d)", str, companyId, maxUses, System.currentTimeMillis()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        joinLinks.add(new JoinLink(str, companyId, maxUses, 0, System.currentTimeMillis()));
        return str;
    }

}
