package xyz.spedcord.server.joinlink;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import xyz.spedcord.common.mongodb.CallbackSubscriber;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.util.CarelessSubscriber;
import xyz.spedcord.server.util.StringUtil;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class JoinLinkController {

    private final MongoDBService mongoDBService;

    private final Set<JoinLink> joinLinks = new HashSet<>();
    private MongoCollection<JoinLink> joinLinkCollection;

    public JoinLinkController(MongoDBService mongoDBService) {
        this.mongoDBService = mongoDBService;
        init();
    }

    private void init() {
        joinLinkCollection = mongoDBService.getDatabase().getCollection("join_links", JoinLink.class);
        load();
    }

    private void load() {
        CallbackSubscriber<JoinLink> subscriber = new CallbackSubscriber<>();
        subscriber.doOnNext(joinLinks::add);

        joinLinkCollection.find().subscribe(subscriber);
    }

    public int getCompanyId(String joinId) {
        return joinLinks.stream()
                .filter(joinLink -> joinLink.getId().equals(joinId))
                .map(JoinLink::getCompanyId)
                .findAny().orElse(-1);
    }

    public void joinLinkUsed(String id) {
        Optional<JoinLink> optional = joinLinks.stream()
                .filter(joinLink -> joinLink.getId().equals(id))
                .findAny();
        if (optional.isEmpty()) {
            return;
        }

        JoinLink joinLink = optional.get();
        joinLink.setUses(joinLink.getUses() + 1);

        if (joinLink.getUses() >= joinLink.getMaxUses() && joinLink.getMaxUses() != -1) {
            removeJoinLink(id);
            return;
        }

        joinLinkCollection.replaceOne(Filters.eq("id", joinLink.getId()), joinLink).subscribe(new CarelessSubscriber<>());
    }

    public void removeJoinLink(String id) {
        new HashSet<>(joinLinks).stream()
                .filter(joinLink -> joinLink.getId().equals(id))
                .findAny()
                .ifPresent(joinLinks::remove);
        joinLinkCollection.deleteOne(Filters.eq("id", id)).subscribe(new CarelessSubscriber<>());
    }

    public String addCustomLink(String str, int companyId, int maxUses) {
        JoinLink joinLink = new JoinLink(str, companyId, maxUses, 0, System.currentTimeMillis());
        joinLinks.add(joinLink);
        joinLinkCollection.insertOne(joinLink).subscribe(new CarelessSubscriber<>());
        return str;
    }

    public String generateNewLink(int companyId, int maxUses) {
        return addCustomLink(UUID.randomUUID().toString().replace("-", ""), companyId, maxUses);
    }

}
