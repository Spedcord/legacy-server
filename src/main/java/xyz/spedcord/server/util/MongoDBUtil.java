package xyz.spedcord.server.util;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import xyz.spedcord.common.mongodb.CallbackSubscriber;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Maximilian Dorn
 * @version 2.1.4
 * @since 1.0.0
 */
public class MongoDBUtil {

    private MongoDBUtil() {
    }

    /**
     * Count all documents in a collection
     *
     * @param collection The collection
     * @return The amount of documents in the collection
     */
    public static long countDocuments(MongoCollection<?> collection) {
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

    /**
     * Find the first free numeric id
     *
     * @param collection The collection
     * @return First free id
     */
    public static int findFirstFreeId(MongoCollection<?> collection) {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            AtomicBoolean found = new AtomicBoolean(false);
            AtomicBoolean finished = new AtomicBoolean(false);
            collection.find(Filters.eq("_id", i)).subscribe(new Subscriber<Object>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(Object o) {
                    found.set(true);
                }

                @Override
                public void onError(Throwable t) {
                }

                @Override
                public void onComplete() {
                    finished.set(true);
                }
            });

            while (!finished.get()) {
            }

            if (!found.get()) {
                return i;
            }
        }
        return -1;
    }

}
