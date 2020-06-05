package xyz.spedcord.server.user;

import java.util.List;

public class User {

    public static final User EMPTY = new User(-1, -1, null, null,
            null, -1, -1, -1, null, null);

    private int id;
    private final long discordId;
    private String key;
    private String accessToken;
    private String refreshToken;
    private long tokenExpires;
    private int companyId;
    private double balance;
    private final List<Integer> jobList;
    private Flag[] flags;

    public User(int id, long discordId, String key, String accessToken, String refreshToken, long tokenExpires, int companyId, double balance, List<Integer> jobList, Flag[] flags) {
        this.id = id;
        this.discordId = discordId;
        this.key = key;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpires = tokenExpires;
        this.companyId = companyId;
        this.balance = balance;
        this.jobList = jobList;
        this.flags = flags;
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

    public int getCompanyId() {
        return companyId;
    }

    public double getBalance() {
        return balance;
    }

    public List<Integer> getJobList() {
        return jobList;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getTokenExpires() {
        return tokenExpires;
    }

    public Flag[] getFlags() {
        return flags;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setTokenExpires(long tokenExpires) {
        this.tokenExpires = tokenExpires;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setFlags(Flag[] flags) {
        this.flags = flags;
    }

    public void setId(int id) {
        this.id = id;
    }

    public enum Flag {
        CHEATER
    }
}
