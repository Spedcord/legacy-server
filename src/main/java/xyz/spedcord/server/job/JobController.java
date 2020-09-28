package xyz.spedcord.server.job;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * A controller that handles everything related to jobs
 *
 * @author Maximilian Dorn
 * @version 2.1.13
 * @since 1.0.0
 */
public class JobController {

    private final Map<Long, Job> pendingJobs = new HashMap<>();

    private final MongoDBService mongoDBService;
    private MongoCollection<Job> jobCollection;

    /**
     * Constructs a new instance of this controller
     *
     * @param mongoDBService The MongoDB service
     */
    public JobController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        this.init();
    }

    /**
     * Initializes this instance
     */
    private void init() {
        // Fetches the collection
        this.jobCollection = this.mongoDBService.getDatabase().getCollection("jobs", Job.class);

        // Recover jobs that were in progress when server shut down
        this.readTempFile();
    }

    /**
     * Starts a new job
     *
     * @param discordId   The Discord id of the driver
     * @param fromCity    The city where the job started
     * @param toCity      The destination city
     * @param truck       The truck make and model
     * @param cargo       The transported cargo
     * @param cargoWeight The weight of the cargo
     */
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

        // Notify webhooks
        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(this.pendingJobs.get(discordId)).getAsJsonObject();
        jsonObject.addProperty("state", "START");
        WebhookUtil.callWebhooks(discordId, jsonObject, "JOB");
    }

    /**
     * Ends the pending job of a user job
     *
     * @param discordId The Discord id of the driver
     * @param pay       The income that the job generated
     */
    public void endJob(long discordId, double pay) {
        // Abort if the user has no pending job
        Job job = this.pendingJobs.remove(discordId);
        if (job == null) {
            return;
        }

        // Set job id, end timestamp and pay
        job.setId(MongoDBUtil.findFirstFreeId(this.jobCollection));
        job.setEndedAt(System.currentTimeMillis());
        job.setPay(pay);

        // Save to database
        this.jobCollection.insertOne(job).subscribe(new CarelessSubscriber<>());

        // Notify webhooks
        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(job).getAsJsonObject();
        jsonObject.addProperty("state", "END");
        WebhookUtil.callWebhooks(discordId, jsonObject, "JOB");
    }

    /**
     * Cancels the pending job of a user
     *
     * @param discordId The Discord id of the driver
     */
    public void cancelJob(long discordId) {
        Job job = this.pendingJobs.remove(discordId);

        // Notify webhooks
        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(job).getAsJsonObject();
        jsonObject.addProperty("state", "CANCEL");
        WebhookUtil.callWebhooks(discordId, jsonObject, "JOB");
    }

    /**
     * Returns the current job of a user
     *
     * @param discordId The Discord id of the driver
     * @return The current job or null
     */
    public Job getPendingJob(long discordId) {
        return this.pendingJobs.get(discordId);
    }

    /**
     * Returns a list of jobs matching the provided job ids
     *
     * @param collection The job ids
     * @return The jobs
     */
    public List<Job> getJobs(List<Integer> collection) {
        return this.getJobs(collection.stream().mapToInt(value -> value).toArray());
    }

    /**
     * Returns a list containing the jobs of every company member
     *
     * @param company        The company
     * @param userController A usercontroller instance
     * @return The job list
     */
    public List<Job> getJobs(Company company, UserController userController) {
        return company.getMemberDiscordIds().stream()
                .map(userController::getUser)
                .map(user -> user.orElse(null))
                .filter(Objects::nonNull)
                .flatMap(user -> this.getJobs(user.getJobList()).stream())
                .sorted(Comparator.comparingLong(value -> ((Job) value).getEndedAt()).reversed())
                .collect(Collectors.toList());
    }

    /**
     * @see JobController#getJobs(List)
     */
    public List<Job> getJobs(int... ids) {
        List<Job> list = new ArrayList<>();

        if (ids.length == 0) {
            return list;
        }

        // Setup subscriber
        AtomicBoolean finished = new AtomicBoolean(false);
        CallbackSubscriber<Job> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(list::add);
        subscriber.doOnComplete(() -> finished.set(true));

        // Block while operation is not finished
        // Thanks bson
        this.jobCollection.find(Filters.all("_id", Arrays.stream(ids).boxed().collect(Collectors.toList()))).subscribe(subscriber);
        while (!finished.get()) {
        }

        return list;
    }

    /**
     * Returns the job with the corresponding id
     *
     * @param id The job id
     * @return The job
     */
    public Job getJob(int id) {
        return this.getJobs(id).stream().findAny().orElse(null);
    }

    /**
     * Saves the job changes
     *
     * @param job The job
     */
    public void updateJob(Job job) {
        this.jobCollection.replaceOne(Filters.eq("_id", job.getId()), job).subscribe(new CarelessSubscriber<>());
    }

    private void readTempFile() {
        File file = new File(".temp-jobs");
        if (!file.exists()) {
            return;
        }

        try {
            String fileContent = Files.readString(file.toPath());
            JsonArray array = JsonParser.parseString(fileContent).getAsJsonArray();
            array.forEach(element -> {
                JsonObject object = element.getAsJsonObject();
                long user = object.get("user").getAsLong();
                Job job = SpedcordServer.GSON.fromJson(object.get("job"), Job.class);

                this.pendingJobs.put(user, job);
            });

            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Recovered " + this.pendingJobs.size() + " pending jobs");
    }

    public void shutdown() {
        if (this.pendingJobs.isEmpty()) {
            return;
        }

        try {
            File file = new File(".temp-jobs");
            file.createNewFile();

            JsonArray array = new JsonArray();
            this.pendingJobs.forEach((user, job) -> {
                JsonObject object = new JsonObject();
                object.addProperty("user", user);
                object.add("job", SpedcordServer.GSON.toJsonTree(job));
                array.add(object);
            });

            FileOutputStream out = new FileOutputStream(file);
            out.write(array.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Returns a list containing every job that was not verified yet
    public List<Job> getUnverifiedJobs() {
        List<Job> list = new ArrayList<>();

        // Setup subscriber
        AtomicBoolean finished = new AtomicBoolean(false);
        CallbackSubscriber<Job> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(list::add);
        subscriber.doOnComplete(() -> finished.set(true));

        // Block while operation is not finished
        this.jobCollection.find(Filters.eq("verifyState", 0)).subscribe(subscriber);
        while (!finished.get()) {
        }

        list.sort(Comparator.comparingLong(Job::getEndedAt));
        return list;
    }

    /**
     * Returns true if the user can start a job
     *
     * @param discordId The Discord id of the user
     * @return true if user can start job
     */
    public boolean canStartJob(long discordId) {
        return !this.pendingJobs.containsKey(discordId);
    }

}
