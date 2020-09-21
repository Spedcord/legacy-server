package xyz.spedcord.server.statistics;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.util.CarelessSubscriber;
import xyz.spedcord.server.util.MongoDBUtil;

/**
 * A controller that handles everything related to statistics
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class StatisticsController {

    private final MongoDBService mongoDBService;
    private MongoCollection<Statistics> statsCollection;
    private Statistics statistics;

    /**
     * Constructs a new instance of this controller
     *
     * @param mongoDBService The MongoDB service
     */
    public StatisticsController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        this.init();
    }

    /**
     * Initializes this controller instance
     */
    private void init() {
        this.statsCollection = this.mongoDBService.getDatabase().getCollection("statistics", Statistics.class);

        long docs = MongoDBUtil.countDocuments(this.statsCollection);
        if (docs == 0) {
            // Create new stats instance if none is present in the database
            this.statistics = new Statistics(0, 0, 0, 0, 0);
            this.statsCollection.insertOne(this.statistics).subscribe(new CarelessSubscriber<>());
        } else {
            // Get stats from database
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

    /**
     * Save stats changes to database
     */
    public void update() {
        this.statsCollection.replaceOne(Filters.eq("obligatoryId", this.statistics.getObligatoryId()), this.statistics).subscribe(new CarelessSubscriber<>());
    }

    /**
     * Returns the stats
     *
     * @return The stats
     */
    public Statistics getStatistics() {
        return this.statistics;
    }

}
