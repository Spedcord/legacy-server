package xyz.spedcord.server.user;

import com.google.gson.JsonObject;
import xyz.spedcord.common.sql.MySqlService;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.util.StringUtil;
import xyz.spedcord.server.util.WebhookUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class UserController {

    private final MySqlService mySqlService;

    private final Set<User> users = new HashSet<>();

    public UserController(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
        init();
    }

    private void init() {
        try {
            mySqlService.update("CREATE TABLE IF NOT EXISTS users (id BIGINT AUTO_INCREMENT, discordId BIGINT, " +
                    "ukey VARCHAR(64), accessToken VARCHAR(128), refreshToken VARCHAR(128), companyId BIGINT, " +
                    "jobs MEDIUMTEXT, PRIMARY KEY (id))");
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() throws SQLException {
        users.clear();

        ResultSet resultSet = mySqlService.execute("SELECT * FROM users");
        while (resultSet.next()) {
            users.add(new User(
                    resultSet.getInt("id"),
                    resultSet.getLong("discordId"),
                    resultSet.getString("ukey"),
                    resultSet.getString("accessToken"),
                    resultSet.getString("refreshToken"),
                    resultSet.getInt("companyId"),
                    Arrays.stream(resultSet.getString("jobs").split(";"))
                            .filter(s -> !s.matches("\\s+") && !s.equals(""))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList())
            ));
        }
    }

    public Optional<User> getUser(long discordId) {
        return users.stream().filter(user -> user.getDiscordId() == discordId).findAny();
    }

    public void createUser(long discordId, String accessToken, String refreshToken) {
        try {
            mySqlService.update(String.format("INSERT INTO users (discordId, ukey, accessToken, refreshToken, companyId, jobs) " +
                    "VALUES (%d, '%s', '%s', '%s', -1, '')", discordId, StringUtil.generateKey(32), accessToken, refreshToken));
            ResultSet resultSet = mySqlService.execute(String.format("SELECT * FROM users WHERE discordId = %d", discordId));
            if (resultSet.next()) {
                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getLong("discordId"),
                        resultSet.getString("ukey"),
                        resultSet.getString("accessToken"),
                        resultSet.getString("refreshToken"),
                        resultSet.getInt("companyId"),
                        Arrays.stream(resultSet.getString("jobs").split(";"))
                                .filter(s -> !s.matches("\\s+") && !s.equals(""))
                                .map(Integer::parseInt)
                                .collect(Collectors.toList())
                );
                users.add(user);

                JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(user).getAsJsonObject();
                WebhookUtil.callWebhooks(discordId, jsonObject, "NEW_USER");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUser(User user) {
        try {
            mySqlService.update(String.format("UPDATE users SET companyId = %d, jobs = '%s' WHERE discordId = %d",
                    user.getCompanyId(), user.getJobList().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(";")),
                    user.getDiscordId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changeKey(User user) {
        user.setKey(StringUtil.generateKey(32));
    }

}
