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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserController {

    private final MongoDBService mongoDBService;

    private final Set<User> users = new HashSet<>();
    private MongoCollection<User> userCollection;

    public UserController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        this.init();
    }

    private void init() {
        this.userCollection = this.mongoDBService.getDatabase().getCollection("users", User.class);
        this.loadUsers();
    }

    private void loadUsers() {
        this.users.clear();

        AtomicBoolean finished = new AtomicBoolean(false);
        CallbackSubscriber<User> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(this.users::add);
        subscriber.doOnComplete(() -> finished.set(true));

        this.userCollection.find().subscribe(subscriber);
        while (!finished.get()) {
        }
    }

    public Optional<User> getUser(long discordId) {
        return this.users.stream().filter(user -> user.getDiscordId() == discordId).findAny();
    }

    public Optional<User> getUserByJobId(int jobId) {
        return this.users.stream().filter(user -> user.getJobList().contains(jobId)).findAny();
    }

    public void createUser(long discordId, String accessToken, String refreshToken, long tokenExpires) {
        long docs = MongoDBUtil.countDocuments(this.userCollection);
        User user = new User(Long.valueOf(docs).intValue(), discordId, StringUtil.generateKey(32), accessToken, refreshToken, tokenExpires, -1, 0, new ArrayList<>(), new ArrayList<>());
        this.userCollection.insertOne(user).subscribe(new CarelessSubscriber<>());
        this.users.add(user);

        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(user).getAsJsonObject();
        WebhookUtil.callWebhooks(discordId, jsonObject, "NEW_USER");
    }

    public void updateUser(User user) {
        this.userCollection.replaceOne(Filters.eq("_id", user.getId()), user).subscribe(new CarelessSubscriber<>());
    }

    public void changeKey(User user) {
        user.setKey(StringUtil.generateKey(32));
    }

    public Set<User> getUsers() {
        return this.users;
    }

}
