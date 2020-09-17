package xyz.spedcord.server.joinlink;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class JoinLink {

    private final String id;
    private final int companyId;
    private final int maxUses;
    private int uses;
    private final long createdAt;

    @BsonCreator
    public JoinLink(@BsonId String id, @BsonProperty("companyId") int companyId, @BsonProperty("maxUses") int maxUses, @BsonProperty("uses") int uses, @BsonProperty("createdAt") long createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.maxUses = maxUses;
        this.uses = uses;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getUses() {
        return uses;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

}
