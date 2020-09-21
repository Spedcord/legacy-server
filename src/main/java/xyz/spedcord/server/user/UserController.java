package xyz.spedcord.server.user;

import com.google.gson.JsonObject;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import xyz.spedcord.common.mongodb.CallbackSubscriber;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.util.CarelessSubscriber;
import xyz.spedcord.server.util.MongoDBUtil;
import xyz.spedcord.server.util.StringUtil;
import xyz.spedcord.server.util.WebhookUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A controller that handles everything related to users
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class UserController {

    private final Set<User> users = new HashSet<>();

    private final MongoDBService mongoDBService;
    private MongoCollection<User> userCollection;

    /**
     * Constructs a new instance of this controller
     *
     * @param mongoDBService The MongoDB service
     */
    public UserController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        this.init();
    }

    /**
     * Initializes this instance
     */
    private void init() {
        // Fetch user collection and load users into a list
        this.userCollection = this.mongoDBService.getDatabase().getCollection("users", User.class);
        this.loadUsers();
    }

    private void loadUsers() {
        // Clear list
        this.users.clear();

        // Set up the subscriber
        AtomicBoolean finished = new AtomicBoolean(false);
        CallbackSubscriber<User> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(this.users::add);
        subscriber.doOnComplete(() -> finished.set(true));

        // Fetch users and block while operation is not complete
        this.userCollection.find().subscribe(subscriber);
        while (!finished.get()) {
        }
    }

    /**
     * Tries to fetch a user with the corresponding Discord id
     *
     * @param discordId The users Discord id
     * @return The user with the corresponding id or none
     */
    public Optional<User> getUser(long discordId) {
        return this.users.stream().filter(user -> user.getDiscordId() == discordId).findAny();
    }

    /**
     * Tries to fetch a user by a job id
     *
     * @param jobId The job id
     * @return The user that completed the job or none
     */
    public Optional<User> getUserByJobId(int jobId) {
        return this.users.stream().filter(user -> user.getJobList().contains(jobId)).findAny();
    }

    /**
     * Creates a new user
     *
     * @param discordId    The users Discord id
     * @param accessToken  The users Discord access token
     * @param refreshToken The users Discord refresh token
     * @param tokenExpires The timestamp when the token expires
     */
    public void createUser(long discordId, String accessToken, String refreshToken, long tokenExpires) {
        // Create user and save to database
        long docs = MongoDBUtil.countDocuments(this.userCollection);
        User user = new User(Long.valueOf(docs).intValue(), discordId, StringUtil.generateKey(32), accessToken, refreshToken, tokenExpires, -1, 0, new ArrayList<>(), new ArrayList<>(), User.AccountType.USER.getVal());
        this.userCollection.insertOne(user).subscribe(new CarelessSubscriber<>());
        this.users.add(user);

        // Notify webhooks
        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(user).getAsJsonObject();
        WebhookUtil.callWebhooks(discordId, jsonObject, "NEW_USER");
    }

    /**
     * Saves the changes made to the user instance
     *
     * @param user The user
     */
    public void updateUser(User user) {
        this.userCollection.replaceOne(Filters.eq("_id", user.getId()), user).subscribe(new CarelessSubscriber<>());
    }

    /**
     * Generates a new key for the user
     *
     * @param user The user
     */
    public void changeKey(User user) {
        user.setKey(StringUtil.generateKey(32));
    }

    /**
     * Returns a unmodifiable set containing every loaded user
     *
     * @return All loaded users
     */
    public Set<User> getUsers() {
        return Collections.unmodifiableSet(this.users);
    }

}
