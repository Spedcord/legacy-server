package xyz.spedcord.server.user;

import xyz.spedcord.common.sql.MySqlService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class UserController {

    private final MySqlService mySqlService;

    private final Set<User> users = new HashSet<>();

    public UserController(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
        init();
    }

    private void init() {
        try {
            mySqlService.update("CREATE TABLE IF NOT EXISTS users (id BIGINT AUTO_INCREMENT, discordId BIGINT, ukey VARCHAR(64), companyId VARCHAR(64), PRIMARY KEY (id))");
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
                    resultSet.getString("companyId")
            ));
        }
    }

    private void loadUser(long discordId) throws SQLException {
        ResultSet resultSet = mySqlService.execute(String.format("SELECT * FROM users WHERE discordId = %d", discordId));
        while (resultSet.next()) {
            users.add(new User(
                    resultSet.getInt("id"),
                    resultSet.getLong("discordId"),
                    resultSet.getString("key"),
                    resultSet.getString("companyId")
            ));
        }
    }

    public Optional<User> getUser(long discordId) {
        return users.stream().filter(user -> user.getDiscordId() == discordId).findAny();
    }

    public void createUser(long discordId) {
        try {
            mySqlService.update(String.format("INSERT INTO users (discordId, key, companyId) VALUES (%d, '%s', '')", discordId, generateKey()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
