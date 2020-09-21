package xyz.spedcord.server.util;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class CarelessSubscriber<T> implements Subscriber<T> {
    @Override
    public void onSubscribe(Subscription s) {
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T t) {
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void onComplete() {
    }
}
