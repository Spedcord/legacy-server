package xyz.spedcord.server.statistics;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.util.CarelessSubscriber;
import xyz.spedcord.server.util.MongoDBUtil;

public class StatisticsController {

    private final MongoDBService mongoDBService;
    private MongoCollection<Statistics> statsCollection;
    private Statistics statistics;

    public StatisticsController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        init();
    }

    private void init() {
        statsCollection = mongoDBService.getDatabase().getCollection("statistics", Statistics.class);

        long docs = MongoDBUtil.countDocuments(statsCollection);
        if (docs == 0) {
            statistics = new Statistics(0, 0, 0, 0, 0);
            statsCollection.insertOne(statistics).subscribe(new CarelessSubscriber<>());
        } else {
            statsCollection.find().subscribe(new Subscriber<>() {
                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(Statistics statistics) {
                    StatisticsController.this.statistics = statistics;
                }

                @Override
                public void onError(Throwable t) {
                }

                @Override
                public void onComplete() {
                }
            });
        }
    }

    public void update() {
        statsCollection.replaceOne(Filters.eq("obligatoryId", statistics.getObligatoryId()), statistics).subscribe(new CarelessSubscriber<>());
    }

    public Statistics getStatistics() {
        return statistics;
    }

}
