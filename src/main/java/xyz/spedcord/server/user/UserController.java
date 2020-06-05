package xyz.spedcord.server.user;

import com.google.gson.JsonObject;
import xyz.spedcord.common.sql.MySqlService;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.util.StringUtil;
import xyz.spedcord.server.util.WebhookUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
                    "ukey VARCHAR(64), accessToken VARCHAR(128), refreshToken VARCHAR(128), tokenExpires BIGINT, " +
                    "balance DOUBLE, companyId BIGINT, jobs MEDIUMTEXT, flags TINYTEXT, PRIMARY KEY (id))");
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
                    resultSet.getLong("tokenExpires"),
                    resultSet.getInt("companyId"),
                    resultSet.getDouble("balance"),
                    Arrays.stream(resultSet.getString("jobs").split(";"))
                            .filter(s -> !s.matches("\\s+") && !s.equals(""))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList()),
                    Arrays.stream(resultSet.getString("flags").split(";"))
                            .filter(s -> !s.matches("\\s+") && !s.equals(""))
                            .map(User.Flag::valueOf)
                            .toArray(User.Flag[]::new)
            ));
        }
    }

    public Optional<User> getUser(long discordId) {
        return users.stream().filter(user -> user.getDiscordId() == discordId).findAny();
    }

    public Optional<User> getUserByJobId(int jobId) {
        return users.stream().filter(user -> user.getJobList().contains(jobId)).findAny();
    }

    public void createUser(long discordId, String accessToken, String refreshToken, long tokenExpires) {
        try {
            mySqlService.update(String.format("INSERT INTO users (discordId, ukey, accessToken, refreshToken, tokenExpires, balance, companyId, jobs, flags) " +
                    "VALUES (%d, '%s', '%s', '%s', %d, 0, -1, '', '')", discordId, StringUtil.generateKey(32), accessToken, refreshToken, tokenExpires));
            ResultSet resultSet = mySqlService.execute(String.format("SELECT * FROM users WHERE discordId = %d", discordId));
            if (resultSet.next()) {
                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getLong("discordId"),
                        resultSet.getString("ukey"),
                        resultSet.getString("accessToken"),
                        resultSet.getString("refreshToken"),
                        resultSet.getLong("tokenExpires"),
                        resultSet.getInt("companyId"),
                        resultSet.getDouble("balance"),
                        Arrays.stream(resultSet.getString("jobs").split(";"))
                                .filter(s -> !s.matches("\\s+") && !s.equals(""))
                                .map(Integer::parseInt)
                                .collect(Collectors.toList()),
                        Arrays.stream(resultSet.getString("flags").split(";"))
                                .filter(s -> !s.matches("\\s+") && !s.equals(""))
                                .map(User.Flag::valueOf)
                                .toArray(User.Flag[]::new)
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
            mySqlService.update(String.format("UPDATE users SET companyId = %d, jobs = '%s', accessToken = '%s', " +
                            "refreshToken = '%s', tokenExpires = %d, balance = %f, ukey = '%s', flags = '%s' WHERE discordId = %d",
                    user.getCompanyId(), user.getJobList().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(";")),
                    user.getAccessToken(), user.getRefreshToken(), user.getTokenExpires(),
                    user.getBalance(), user.getKey(), Arrays.stream(user.getFlags())
                            .map(Enum::name).collect(Collectors.joining(";")), user.getDiscordId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changeKey(User user) {
        user.setKey(StringUtil.generateKey(32));
    }

    public Set<User> getUsers() {
        return users;
    }

}
