package xyz.spedcord.server.job;

import com.google.gson.JsonObject;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import xyz.spedcord.common.mongodb.CallbackSubscriber;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.util.CarelessSubscriber;
import xyz.spedcord.server.util.WebhookUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class JobController {

    private final MongoDBService mongoDBService;

    private final Map<Long, Job> pendingJobs = new HashMap<>();
    private MongoCollection<Job> jobCollection;

    public JobController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        init();
    }

    private void init() {
        jobCollection = mongoDBService.getDatabase().getCollection("jobs", Job.class);
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

        jobCollection.insertOne(job).subscribe(new CarelessSubscriber<>());

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

        AtomicBoolean finished = new AtomicBoolean(false);
        CallbackSubscriber<Job> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(list::add);
        subscriber.doOnComplete(() -> finished.set(true));

        jobCollection.find(Filters.all("id", ids)).subscribe(subscriber);
        while (!finished.get()) ;

        return list;
    }

    public Job getJob(int id) {
        return getJobs(id).stream().findAny().orElse(null);
    }

    public void updateJob(Job job) {
        jobCollection.replaceOne(Filters.eq("id", job.getId()), job);
    }

    public List<Job> getUnverifiedJobs() {
        List<Job> list = new ArrayList<>();

        CallbackSubscriber<Job> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(list::add);

        jobCollection.find(Filters.eq("verified", false)).subscribe(subscriber);

        return list;
    }

    public boolean canStartJob(long discordId) {
        return !pendingJobs.containsKey(discordId);
    }

}
