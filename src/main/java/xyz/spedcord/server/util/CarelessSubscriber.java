package xyz.spedcord.server.util;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

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