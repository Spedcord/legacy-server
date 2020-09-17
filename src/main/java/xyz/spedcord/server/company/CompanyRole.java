package xyz.spedcord.server.company;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CompanyRole {

    private String name;
    private double payout;
    private final List<Long> memberDiscordIds;

    @BsonCreator
    public CompanyRole(@BsonProperty("name") String name, @BsonProperty("payout") double payout, @BsonProperty("memberDiscordIds") List<Long> memberDiscordIds) {
        this.name = name;
        this.payout = payout;
        this.memberDiscordIds = memberDiscordIds;
    }

    public static CompanyRole createDefault() {
        return new CompanyRole("Default", 1000, new ArrayList<>());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPayout() {
        return payout;
    }

    public void setPayout(double payout) {
        this.payout = payout;
    }

    public List<Long> getMemberDiscordIds() {
        return memberDiscordIds;
    }

}
