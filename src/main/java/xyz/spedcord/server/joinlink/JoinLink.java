package xyz.spedcord.server.joinlink;

public class JoinLink {

    private final String id;
    private final int companyId;
    private final int maxUses;
    private int uses;
    private final long createdAt;

    public JoinLink(String id, int companyId, int maxUses, int uses, long createdAt) {
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
