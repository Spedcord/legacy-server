package xyz.spedcord.server.user;

public class User {

    private final int id;
    private final long discordId;
    private final String key;
    private final String companyId;

    public User(int id, long discordId, String key, String companyId) {
        this.id = id;
        this.discordId = discordId;
        this.key = key;
        this.companyId = companyId;
    }

    public int getId() {
        return id;
    }

    public long getDiscordId() {
        return discordId;
    }

    public String getKey() {
        return key;
    }

    public String getCompanyId() {
        return companyId;
    }

}
