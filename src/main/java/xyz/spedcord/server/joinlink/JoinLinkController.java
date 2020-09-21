package xyz.spedcord.server.joinlink;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import xyz.spedcord.common.mongodb.CallbackSubscriber;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.util.CarelessSubscriber;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A controller that handles everything related to join links
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class JoinLinkController {

    private final Set<JoinLink> joinLinks = new HashSet<>();

    private final MongoDBService mongoDBService;
    private MongoCollection<JoinLink> joinLinkCollection;

    /**
     * Constructs a new instance of this controller
     *
     * @param mongoDBService The MongoDB service
     */
    public JoinLinkController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        this.init();
    }

    /**
     * Initializes this instance
     */
    private void init() {
        // Fetch collection and load join links
        this.joinLinkCollection = this.mongoDBService.getDatabase().getCollection("join_links", JoinLink.class);
        this.load();
    }

    /**
     * Loads all the join links from the collection into a map
     */
    private void load() {
        CallbackSubscriber<JoinLink> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(this.joinLinks::add);

        this.joinLinkCollection.find().subscribe(subscriber);
    }

    /**
     * Retrieves the company id that corresponds to the provided join link id
     *
     * @param joinId The join link id
     * @return The corresponding company id or -1 if none is found
     */
    public int getCompanyId(String joinId) {
        return this.joinLinks.stream()
                .filter(joinLink -> joinLink.getId().equals(joinId))
                .map(JoinLink::getCompanyId)
                .findAny().orElse(-1);
    }

    /**
     * Indicates that the join link with the provided id was used
     *
     * @param id The id of the join link
     */
    public void joinLinkUsed(String id) {
        // Tries to find the join link
        Optional<JoinLink> optional = this.joinLinks.stream()
                .filter(joinLink -> joinLink.getId().equals(id))
                .findAny();
        if (optional.isEmpty()) {
            return;
        }

        // Increase join link uses
        JoinLink joinLink = optional.get();
        joinLink.setUses(joinLink.getUses() + 1);

        // Remove join link if max uses was reached
        if (joinLink.getUses() >= joinLink.getMaxUses() && joinLink.getMaxUses() != -1) {
            this.removeJoinLink(id);
            return;
        }

        // Save changes
        this.joinLinkCollection.replaceOne(Filters.eq("_id", joinLink.getId()), joinLink).subscribe(new CarelessSubscriber<>());
    }

    /**
     * Removes the join link with the corresponding id
     *
     * @param id The join link id
     */
    public void removeJoinLink(String id) {
        new HashSet<>(this.joinLinks).stream()
                .filter(joinLink -> joinLink.getId().equals(id))
                .findAny()
                .ifPresent(this.joinLinks::remove);
        this.joinLinkCollection.deleteOne(Filters.eq("id", id)).subscribe(new CarelessSubscriber<>());
    }

    /**
     * Creates a join link with the provided id
     *
     * @param str       The join link id
     * @param companyId The company id
     * @param maxUses   The max uses
     * @return The join link id
     */
    public String addCustomLink(String str, int companyId, int maxUses) {
        JoinLink joinLink = new JoinLink(str, companyId, maxUses, 0, System.currentTimeMillis());
        this.joinLinks.add(joinLink);
        this.joinLinkCollection.insertOne(joinLink).subscribe(new CarelessSubscriber<>());
        return str;
    }

    /**
     * Generates a join link with a random id
     *
     * @param companyId The company id
     * @param maxUses   The max uses
     * @return The join link id
     */
    public String generateNewLink(int companyId, int maxUses) {
        return this.addCustomLink(UUID.randomUUID().toString().replace("-", ""), companyId, maxUses);
    }

}
