package xyz.spedcord.server.company;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.List;
import java.util.Optional;

public class Company {

    private int id;
    private final long discordServerId;
    private String name;
    private final long ownerDiscordId;
    private double balance;
    private final List<Long> memberDiscordIds;
    private final List<CompanyRole> roles;
    private String defaultRole;

    @BsonCreator
    public Company(@BsonId int id, @BsonProperty("discordServerId") long discordServerId,@BsonProperty("name") String name, @BsonProperty("ownerDiscordId")long ownerDiscordId, @BsonProperty("balance")double balance, @BsonProperty("memberDiscordIds")List<Long> memberDiscordIds, @BsonProperty("roles")List<CompanyRole> roles, @BsonProperty("defaultRole")String defaultRole) {
        this.id = id;
        this.discordServerId = discordServerId;
        this.name = name;
        this.ownerDiscordId = ownerDiscordId;
        this.balance = balance;
        this.memberDiscordIds = memberDiscordIds;
        this.roles = roles;
        this.defaultRole = defaultRole;
    }

    public boolean changeDefaultRole(String newDefaultRole) {
        Optional<CompanyRole> optional = roles.stream()
                .filter(companyRole -> companyRole.getName().equals(newDefaultRole))
                .findAny();
        if(optional.isEmpty()) {
            return false;
        }

        defaultRole = newDefaultRole;
        return true;
    }

    public int getId() {
        return id;
    }

    public long getDiscordServerId() {
        return discordServerId;
    }

    public String getName() {
        return name;
    }

    public long getOwnerDiscordId() {
        return ownerDiscordId;
    }

    public double getBalance() {
        return balance;
    }

    public List<Long> getMemberDiscordIds() {
        return memberDiscordIds;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<CompanyRole> getRoles() {
        return roles;
    }

    public String getDefaultRole() {
        return defaultRole;
    }

}
