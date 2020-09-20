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
        this.init();
    }

    private void init() {
        this.statsCollection = this.mongoDBService.getDatabase().getCollection("statistics", Statistics.class);

        long docs = MongoDBUtil.countDocuments(this.statsCollection);
        if (docs == 0) {
            this.statistics = new Statistics(0, 0, 0, 0, 0);
            this.statsCollection.insertOne(this.statistics).subscribe(new CarelessSubscriber<>());
        } else {
            this.statsCollection.find().subscribe(new Subscriber<>() {
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
        this.statsCollection.replaceOne(Filters.eq("obligatoryId", this.statistics.getObligatoryId()), this.statistics).subscribe(new CarelessSubscriber<>());
    }

    public Statistics getStatistics() {
        return this.statistics;
    }

}
