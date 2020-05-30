package xyz.spedcord.server.job;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import xyz.spedcord.common.sql.MySqlService;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.util.MySqlUtil;
import xyz.spedcord.server.util.WebhookUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JobController {

    private final MySqlService mySqlService;

    private Map<Long, Job> pendingJobs = new HashMap<>();

    public JobController(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
        init();
    }

    private void init() {
        try {
            mySqlService.update("CREATE TABLE IF NOT EXISTS jobs (id BIGINT AUTO_INCREMENT, startedAt BIGINT, " +
                    "endedAt BIGINT, cargoWeight DOUBLE, pay DOUBLE, fromCity TINYTEXT, toCity TINYTEXT, cargo TINYTEXT, " +
                    "truck TINYTEXT, PRIMARY KEY (id))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startJob(long discordId, String fromCity, String toCity, String truck, String cargo, double cargoWeight) {
        pendingJobs.put(discordId, new Job(
                -1,
                System.currentTimeMillis(),
                -1,
                cargoWeight,
                -1,
                fromCity,
                toCity,
                cargo,
                truck,
                new ArrayList<>()
        ));

        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(pendingJobs.get(discordId)).getAsJsonObject();
        jsonObject.addProperty("state", "START");
        WebhookUtil.callWebhooks(discordId, jsonObject);
    }

    public void endJob(long discordId, double pay) {
        Job job = pendingJobs.remove(discordId);
        if (job == null) {
            return;
        }

        job.setEndedAt(System.currentTimeMillis());
        job.setPay(pay);

        try {
            mySqlService.update(String.format("INSERT INTO jobs (startedAt, endedAt, cargoWeight, " +
                            "pay, fromCity, toCity, cargo, truck) VALUES (%d, %d, %f, %f, '%s', '%s', '%s', '%s')",
                    job.getStartedAt(), job.getEndedAt(), job.getCargoWeight(), job.getPay(),
                    MySqlUtil.escapeString(job.getFromCity()), MySqlUtil.escapeString(job.getToCity()),
                    MySqlUtil.escapeString(job.getCargo()), MySqlUtil.escapeString(job.getTruck())));
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(job).getAsJsonObject();
        jsonObject.addProperty("state", "END");
        WebhookUtil.callWebhooks(discordId, jsonObject);
    }

    public void cancelJob(long discordId) {
        Job job = pendingJobs.remove(discordId);
        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(job).getAsJsonObject();
        jsonObject.addProperty("state", "CANCEL");
    }

    public Job getPendingJob(long discordId) {
        return pendingJobs.get(discordId);
    }

    public Job getJob(int id) {
        try {
            ResultSet resultSet = mySqlService.execute(String.format("SELECT * FROM jobs WHERE id = %d", id));
            if (resultSet.next()) {
                return new Job(
                        resultSet.getInt("id"),
                        resultSet.getLong("startedAt"),
                        resultSet.getLong("endedAt"),
                        resultSet.getDouble("cargoWeight"),
                        resultSet.getDouble("pay"),
                        resultSet.getString("fromCity"),
                        resultSet.getString("toCity"),
                        resultSet.getString("cargo"),
                        resultSet.getString("truck"),
                        new ArrayList<>()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean canStartJob(long discordId) {
        return !pendingJobs.containsKey(discordId);
    }
}
