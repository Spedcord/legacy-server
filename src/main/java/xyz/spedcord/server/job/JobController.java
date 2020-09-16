package xyz.spedcord.server.job;

import com.google.gson.JsonObject;
import xyz.spedcord.common.sql.MySqlService;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.util.MySqlUtil;
import xyz.spedcord.server.util.WebhookUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class JobController {

    private final MySqlService mySqlService;

    private final Map<Long, Job> pendingJobs = new HashMap<>();

    public JobController(MySqlService mySqlService) {
        this.mySqlService = mySqlService;
        init();
    }

    private void init() {
        try {
            mySqlService.update("CREATE TABLE IF NOT EXISTS jobs (id BIGINT AUTO_INCREMENT, startedAt BIGINT, " +
                    "endedAt BIGINT, cargoWeight DOUBLE, pay DOUBLE, fromCity TINYTEXT, toCity TINYTEXT, cargo TINYTEXT, " +
                    "truck TINYTEXT, verified INT, PRIMARY KEY (id))");
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
                new ArrayList<>(),
                false
        ));

        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(pendingJobs.get(discordId)).getAsJsonObject();
        jsonObject.addProperty("state", "START");
        WebhookUtil.callWebhooks(discordId, jsonObject, "JOB");
    }

    public void endJob(long discordId, double pay) {
        Job job = pendingJobs.remove(discordId);
        if (job == null) {
            return;
        }

        job.setEndedAt(System.currentTimeMillis());
        job.setPay(pay);

        try {
            ResultSet resultSet = mySqlService.execute("SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_NAME ='jobs'");
            if (resultSet.next()) {
                job.setId(resultSet.getInt(1));
            }

            mySqlService.update(String.format("INSERT INTO jobs (startedAt, endedAt, cargoWeight, " +
                            "pay, fromCity, toCity, cargo, truck, verified) VALUES (%d, %d, %f, %f, '%s', '%s', '%s', '%s', 0)",
                    job.getStartedAt(), job.getEndedAt(), job.getCargoWeight(), job.getPay(),
                    MySqlUtil.escapeString(job.getFromCity()), MySqlUtil.escapeString(job.getToCity()),
                    MySqlUtil.escapeString(job.getCargo()), MySqlUtil.escapeString(job.getTruck())));
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(job).getAsJsonObject();
        jsonObject.addProperty("state", "END");
        WebhookUtil.callWebhooks(discordId, jsonObject, "JOB");
    }

    public void cancelJob(long discordId) {
        Job job = pendingJobs.remove(discordId);
        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(job).getAsJsonObject();
        jsonObject.addProperty("state", "CANCEL");
        WebhookUtil.callWebhooks(discordId, jsonObject, "JOB");
    }

    public Job getPendingJob(long discordId) {
        return pendingJobs.get(discordId);
    }

    public List<Job> getJobs(List<Integer> collection) {
        return getJobs(collection.stream().mapToInt(value -> value).toArray());
    }

    public List<Job> getJobs(int... ids) {
        List<Job> list = new ArrayList<>();

        if (ids.length == 0) {
            return list;
        }

        try {
            ResultSet resultSet = mySqlService.execute("SELECT * from `jobs` WHERE id " + (ids.length == 1 ? " = "
                    + ids[0] : "IN (" + Arrays.stream(ids).mapToObj(String::valueOf).collect(Collectors.joining(", ")) + ")"));
            while (resultSet.next()) {
                list.add(new Job(
                        resultSet.getInt("id"),
                        resultSet.getLong("startedAt"),
                        resultSet.getLong("endedAt"),
                        resultSet.getDouble("cargoWeight"),
                        resultSet.getDouble("pay"),
                        resultSet.getString("fromCity"),
                        resultSet.getString("toCity"),
                        resultSet.getString("cargo"),
                        resultSet.getString("truck"),
                        new ArrayList<>(),
                        resultSet.getInt("verified") == 1
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Job getJob(int id) {
        return getJobs(id).stream().findAny().orElse(null);
    }

    public void updateJob(Job job) {
        try {
            mySqlService.update(String.format("UPDATE jobs SET verified = %d WHERE id = %d",
                    job.isVerified() ? 1 : -1, job.getId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Job> getUnverifiedJobs() {
        List<Job> list = new ArrayList<>();
        try {
            ResultSet resultSet = mySqlService.execute("SELECT * FROM jobs WHERE verified = 0");
            while (resultSet.next()) {
                list.add(new Job(
                        resultSet.getInt("id"),
                        resultSet.getLong("startedAt"),
                        resultSet.getLong("endedAt"),
                        resultSet.getDouble("cargoWeight"),
                        resultSet.getDouble("pay"),
                        resultSet.getString("fromCity"),
                        resultSet.getString("toCity"),
                        resultSet.getString("cargo"),
                        resultSet.getString("truck"),
                        new ArrayList<>(),
                        resultSet.getInt("verified") == 1
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean canStartJob(long discordId) {
        return !pendingJobs.containsKey(discordId);
    }
}
