package xyz.spedcord.server.user;

import xyz.spedcord.common.sql.MySqlService;

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
                    "ukey VARCHAR(64), companyId VARCHAR(64), jobs MEDIUMTEXT, PRIMARY KEY (id))");
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
                    resultSet.getString("companyId"),
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

    public void createUser(long discordId) {
        try {
            mySqlService.update(String.format("INSERT INTO users (discordId, key, companyId, jobs) " +
                    "VALUES (%d, '%s', '', '')", discordId, generateKey()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUser(User user) {
        try {
            mySqlService.update(String.format("UPDATE users SET companyId = '%s', jobs = '%s' WHERE discordId = %d",
                    user.getCompanyId(), user.getJobList().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(";")),
                    user.getDiscordId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changeKey(User user) {
        user.setKey(generateKey());
    }

    private String generateKey() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 64; i++) {
            stringBuilder.append(chars.toCharArray()[random.nextInt(chars.length())]);
        }
        return stringBuilder.toString();
    }

}
