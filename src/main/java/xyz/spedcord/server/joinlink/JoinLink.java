package xyz.spedcord.server.joinlink;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class JoinLink {

    private final String id;
    private final int companyId;
    private final int maxUses;
    private final long createdAt;
    private int uses;

    @BsonCreator
    public JoinLink(@BsonId String id, @BsonProperty("companyId") int companyId, @BsonProperty("maxUses") int maxUses, @BsonProperty("uses") int uses, @BsonProperty("createdAt") long createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.maxUses = maxUses;
        this.uses = uses;
        this.createdAt = createdAt;
    }

    public String getId() {
        return this.id;
    }

    public int getCompanyId() {
        return this.companyId;
    }

    public int getMaxUses() {
        return this.maxUses;
    }

    public int getUses() {
        return this.uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

}
