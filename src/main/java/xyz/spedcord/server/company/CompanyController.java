package xyz.spedcord.server.company;

import xyz.spedcord.common.sql.MySqlService;
import xyz.spedcord.server.util.MySqlUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CompanyController {

    private MySqlService mySqlService;

    private Set<Company> companies = new HashSet<>();

    public CompanyController(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
        init();
    }

    private void init() {
        try {
            mySqlService.update("CREATE TABLE IF NOT EXISTS companies (id BIGINT AUTO_INCREMENT, discordServerId BIGINT, " +
                    "name VARCHAR(128), ownerDiscordId BIGINT, members MEDIUMTEXT, PRIMARY KEY (id))");
            load();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void load() throws SQLException {
        ResultSet resultSet = mySqlService.execute("SELECT * FROM companies");
        while (resultSet.next()) {
            companies.add(new Company(
                    resultSet.getInt("id"),
                    resultSet.getLong("discordServerId"),
                    resultSet.getString("name"),
                    resultSet.getLong("ownerDiscordId"),
                    Arrays.stream(resultSet.getString("members").split(";"))
                            .filter(s -> !s.matches("\\s+") && !s.equals(""))
                            .map(Long::parseLong)
                            .collect(Collectors.toList())
            ));
        }
    }

    public Optional<Company> getCompany(long discordServerId) {
        return companies.stream().filter(company -> company.getDiscordServerId() == discordServerId).findAny();
    }

    public Optional<Company> getCompany(int id) {
        return companies.stream().filter(company -> company.getId() == id).findAny();
    }

    public void createCompany(Company company) {
        try {
            ResultSet resultSet = mySqlService.execute("SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_NAME ='companies'");
            if(resultSet.next()) {
                company.setId(resultSet.getInt(1));
            }

            mySqlService.update(String.format("INSERT INTO companies (discordServerId, name, ownerDiscordId, members) VALUES(%d, '%s', %d, '%s')",
                    company.getDiscordServerId(), MySqlUtil.escapeString(company.getName()), company.getOwnerDiscordId(),
                    company.getMemberDiscordIds().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(";"))));
            companies.add(company);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCompany(Company company) {
        try {
            mySqlService.update(String.format("UPDATE companies SET name = '%s', members = '%s' WHERE id = %d",
                    MySqlUtil.escapeString(company.getName()), company.getMemberDiscordIds().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(";")), company.getId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
