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

public class JoinLinkController {

    private final MongoDBService mongoDBService;

    private final Set<JoinLink> joinLinks = new HashSet<>();
    private MongoCollection<JoinLink> joinLinkCollection;

    public JoinLinkController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        this.init();
    }

    private void init() {
        this.joinLinkCollection = this.mongoDBService.getDatabase().getCollection("join_links", JoinLink.class);
        this.load();
    }

    private void load() {
        CallbackSubscriber<JoinLink> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(this.joinLinks::add);

        this.joinLinkCollection.find().subscribe(subscriber);
    }

    public int getCompanyId(String joinId) {
        return this.joinLinks.stream()
                .filter(joinLink -> joinLink.getId().equals(joinId))
                .map(JoinLink::getCompanyId)
                .findAny().orElse(-1);
    }

    public void joinLinkUsed(String id) {
        Optional<JoinLink> optional = this.joinLinks.stream()
                .filter(joinLink -> joinLink.getId().equals(id))
                .findAny();
        if (optional.isEmpty()) {
            return;
        }

        JoinLink joinLink = optional.get();
        joinLink.setUses(joinLink.getUses() + 1);

        if (joinLink.getUses() >= joinLink.getMaxUses() && joinLink.getMaxUses() != -1) {
            this.removeJoinLink(id);
            return;
        }

        this.joinLinkCollection.replaceOne(Filters.eq("_id", joinLink.getId()), joinLink).subscribe(new CarelessSubscriber<>());
    }

    public void removeJoinLink(String id) {
        new HashSet<>(this.joinLinks).stream()
                .filter(joinLink -> joinLink.getId().equals(id))
                .findAny()
                .ifPresent(this.joinLinks::remove);
        this.joinLinkCollection.deleteOne(Filters.eq("id", id)).subscribe(new CarelessSubscriber<>());
    }

    public String addCustomLink(String str, int companyId, int maxUses) {
        JoinLink joinLink = new JoinLink(str, companyId, maxUses, 0, System.currentTimeMillis());
        this.joinLinks.add(joinLink);
        this.joinLinkCollection.insertOne(joinLink).subscribe(new CarelessSubscriber<>());
        return str;
    }

    public String generateNewLink(int companyId, int maxUses) {
        return this.addCustomLink(UUID.randomUUID().toString().replace("-", ""), companyId, maxUses);
    }

}
