package xyz.spedcord.server.oauth;

import xyz.spedcord.common.sql.MySqlService;
import xyz.spedcord.server.util.MySqlUtil;
import xyz.spedcord.server.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JoinLinkRetriever {

    private final MySqlService mySqlService;

    public JoinLinkRetriever(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
        init();
    }

    private void init() {
        try {
            mySqlService.update("CREATE TABLE IF NOT EXISTS joinlinks (id BIGINT AUTO_INCREMENT, stringId VARCHAR(128), companyId BIGINT, PRIMARY KEY (id))");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public int getCompanyId(String joinId) throws SQLException {
        ResultSet resultSet = mySqlService.execute(String.format("SELECT companyId FROM joinlinks WHERE stringId = '%s'", MySqlUtil.escapeString(joinId)));
        if(resultSet.next()) {
            return resultSet.getInt("companyId");
        }
        return -1;
    }

    public void removeJoinLink(String id) {
        try {
            mySqlService.update(String.format("DELETE FROM joinlinks WHERE stringId = '%s'", MySqlUtil.escapeString(id)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String generateNewLink(int companyId) {
        String str = StringUtil.generateKey(12);
        try {
            mySqlService.update(String.format("INSERT INTO joinlinks (stringId, companyId) VALUES('%s', %d)", str, companyId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return str;
    }

}
