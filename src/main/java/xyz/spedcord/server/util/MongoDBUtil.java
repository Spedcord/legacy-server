package xyz.spedcord.server.util;

import com.mongodb.reactivestreams.client.MongoCollection;
import xyz.spedcord.common.mongodb.CallbackSubscriber;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MongoDBUtil {

    private MongoDBUtil() {
    }

    public static <T> long countDocuments(MongoCollection<T> collection) {
        AtomicLong size = new AtomicLong(0);
        AtomicBoolean finished = new AtomicBoolean(false);
        CallbackSubscriber<Long> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(size::set);
        subscriber.doOnComplete(() -> finished.set(true));

        collection.countDocuments().subscribe(subscriber);
        while (!finished.get()) {
        }

        return size.get();
    }

}
