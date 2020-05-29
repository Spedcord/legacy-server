package xyz.spedcord.server.oauth;

import xyz.spedcord.common.sql.MySqlService;
import xyz.spedcord.server.util.MySqlUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JoinLinkRetriever {

    private MySqlService mySqlService;

    public JoinLinkRetriever(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
        init();
    }

    private void init() {
        try {
            mySqlService.update("CREATE TABLE IF NOT EXISTS joinlinks (id BIGINT AUTO_INCREMENT, stringId VARCHAR(128), companyId VARCHAR(128), PRIMARY KEY (id))");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public String getCompanyId(String joinId) throws SQLException {
        ResultSet resultSet = mySqlService.execute(String.format("SELECT companyId FROM joinlinks WHERE stringId = '%s'", MySqlUtil.escapeString(joinId)));
        if(resultSet.next()) {
            return resultSet.getString("companyId");
        }
        return null;
    }

    public void removeJoinLink(String id) {
        try {
            mySqlService.update(String.format("DELETE FROM joinlinks WHERE stringId = '%s'", MySqlUtil.escapeString(id)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
