package xyz.spedcord.server.user;

import com.google.gson.JsonObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import xyz.spedcord.common.mongodb.CallbackSubscriber;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.util.CarelessSubscriber;
import xyz.spedcord.server.util.StringUtil;
import xyz.spedcord.server.util.WebhookUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class UserController {

    private final MongoDBService mongoDBService;

    private final Set<User> users = new HashSet<>();
    private MongoCollection<User> userCollection;

    public UserController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        init();
    }

    private void init() {
        userCollection = mongoDBService.getDatabase().getCollection("users", User.class);
        loadUsers();
    }

    private void loadUsers() {
        users.clear();

        AtomicBoolean finished = new AtomicBoolean(false);
        CallbackSubscriber<User> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(user -> {
            System.out.println("Next");
            users.add(user);
        });
        subscriber.doOnComplete(() -> {
            System.out.println("Completed");
            finished.set(true);
        });

        userCollection.find().subscribe(subscriber);
        while (!finished.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Optional<User> getUser(long discordId) {
        return users.stream().filter(user -> user.getDiscordId() == discordId).findAny();
    }

    public Optional<User> getUserByJobId(int jobId) {
        return users.stream().filter(user -> user.getJobList().contains(jobId)).findAny();
    }

    public void createUser(long discordId, String accessToken, String refreshToken, long tokenExpires) {
        AtomicLong size = new AtomicLong(0);
        AtomicBoolean finished = new AtomicBoolean(false);
        CallbackSubscriber<Long> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(size::set);
        subscriber.doOnComplete(() -> finished.set(true));

        userCollection.countDocuments().subscribe(subscriber);
        while (!finished.get()) ;

        User user = new User(Long.valueOf(size.get()).intValue(), discordId, StringUtil.generateKey(32), accessToken, refreshToken, tokenExpires, -1, 0, new ArrayList<>(), new Flag[0]);
        userCollection.insertOne(user).subscribe(new CarelessSubscriber<>());
        users.add(user);

        JsonObject jsonObject = SpedcordServer.GSON.toJsonTree(user).getAsJsonObject();
        WebhookUtil.callWebhooks(discordId, jsonObject, "NEW_USER");
    }

    public void updateUser(User user) {
        userCollection.replaceOne(Filters.eq("id", user.getId()), user).subscribe(new CarelessSubscriber<>());
    }

    public void changeKey(User user) {
        user.setKey(StringUtil.generateKey(32));
    }

    public Set<User> getUsers() {
        return users;
    }

}
