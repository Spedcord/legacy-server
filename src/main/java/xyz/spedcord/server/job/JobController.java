package xyz.spedcord.server.job;

import com.google.gson.JsonObject;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import xyz.spedcord.common.mongodb.CallbackSubscriber;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.user.UserController;
import xyz.spedcord.server.util.CarelessSubscriber;
import xyz.spedcord.server.util.MongoDBUtil;
import xyz.spedcord.server.util.WebhookUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class JobController {

    private final MongoDBService mongoDBService;

    private final Map<Long, Job> pendingJobs = new HashMap<>();
    private MongoCollection<Job> jobCollection;

    public JobController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        this.init();
    }

    private void init() {
        this.jobCollection = this.mongoDBService.getDatabase().getCollection("jobs", Job.class);
    }

    public void startJob(long discordId, String fromCity, String toCity, String truck, String cargo, double cargoWeight) {
        this.pendingJobs.put(discordId, new Job(
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
                0
        ));

        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(this.pendingJobs.get(discordId)).getAsJsonObject();
        jsonObject.addProperty("state", "START");
        WebhookUtil.callWebhooks(discordId, jsonObject, "JOB");
    }

    public void endJob(long discordId, double pay) {
        Job job = this.pendingJobs.remove(discordId);
        if (job == null) {
            return;
        }

        long docs = MongoDBUtil.countDocuments(this.jobCollection);
        job.setId(Long.valueOf(docs).intValue());
        job.setEndedAt(System.currentTimeMillis());
        job.setPay(pay);

        this.jobCollection.insertOne(job).subscribe(new CarelessSubscriber<>());

        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(job).getAsJsonObject();
        jsonObject.addProperty("state", "END");
        WebhookUtil.callWebhooks(discordId, jsonObject, "JOB");
    }

    public void cancelJob(long discordId) {
        Job job = this.pendingJobs.remove(discordId);
        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(job).getAsJsonObject();
        jsonObject.addProperty("state", "CANCEL");
        WebhookUtil.callWebhooks(discordId, jsonObject, "JOB");
    }

    public Job getPendingJob(long discordId) {
        return this.pendingJobs.get(discordId);
    }

    public List<Job> getJobs(List<Integer> collection) {
        return this.getJobs(collection.stream().mapToInt(value -> value).toArray());
    }

    public List<Job> getJobs(Company company, UserController userController) {
        return company.getMemberDiscordIds().stream()
                .map(userController::getUser)
                .map(user -> user.orElse(null))
                .filter(Objects::nonNull)
                .flatMap(user -> this.getJobs(user.getJobList()).stream())
                .sorted(Comparator.comparingLong(value -> ((Job) value).getEndedAt()).reversed())
                .collect(Collectors.toList());
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

        // Thanks bson
        this.jobCollection.find(Filters.all("_id", Arrays.stream(ids).boxed().collect(Collectors.toList()))).subscribe(subscriber);
        while (!finished.get()) {
        }

        return list;
    }

    public Job getJob(int id) {
        return this.getJobs(id).stream().findAny().orElse(null);
    }

    public void updateJob(Job job) {
        this.jobCollection.replaceOne(Filters.eq("id", job.getId()), job);
    }

    public List<Job> getUnverifiedJobs() {
        List<Job> list = new ArrayList<>();

        AtomicBoolean finished = new AtomicBoolean(false);
        CallbackSubscriber<Job> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(list::add);
        subscriber.doOnComplete(() -> finished.set(true));

        this.jobCollection.find(Filters.eq("verifyState", 0)).subscribe(subscriber);
        while (!finished.get()) {
        }

        list.sort(Comparator.comparingLong(Job::getEndedAt));
        return list;
    }

    public boolean canStartJob(long discordId) {
        return !this.pendingJobs.containsKey(discordId);
    }

}
