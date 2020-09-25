package xyz.spedcord.server.company;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.List;
import java.util.Optional;

/**
 * @author Maximilian Dorn
 * @version 2.1.9
 * @since 1.0.0
 */
public class Company {

    public static final int DEFAULT_MEMBER_LIMIT = 10;

    private final long discordServerId;
    private final long ownerDiscordId;
    private final List<Long> memberDiscordIds;
    private final List<Integer> purchasedItems;
    private final List<CompanyRole> roles;
    private int id;
    private int memberLimit;
    private String name;
    private double balance;
    private String defaultRole;

    @BsonCreator
    public Company(@BsonId int id, @BsonProperty("discordServerId") long discordServerId, @BsonProperty("memberLimit") int memberLimit, @BsonProperty("name") String name, @BsonProperty("ownerDiscordId") long ownerDiscordId, @BsonProperty("balance") double balance, @BsonProperty("memberDiscordIds") List<Long> memberDiscordIds, @BsonProperty("purchasedItems") List<Integer> purchasedItems, @BsonProperty("roles") List<CompanyRole> roles, @BsonProperty("defaultRole") String defaultRole) {
        this.id = id;
        this.discordServerId = discordServerId;
        this.memberLimit = memberLimit;
        this.name = name;
        this.ownerDiscordId = ownerDiscordId;
        this.balance = balance;
        this.memberDiscordIds = memberDiscordIds;
        this.purchasedItems = purchasedItems;
        this.roles = roles;
        this.defaultRole = defaultRole;
    }

    public boolean changeDefaultRole(String newDefaultRole) {
        Optional<CompanyRole> optional = this.roles.stream()
                .filter(companyRole -> companyRole.getName().equals(newDefaultRole))
                .findAny();
        if (optional.isEmpty()) {
            return false;
        }

        this.defaultRole = newDefaultRole;
        return true;
    }

    public boolean hasPermission(long member, CompanyRole.Permission permission) {
        return this.getRole(member).map(companyRole -> companyRole.hasPermission(permission)).orElse(false);
    }

    public Optional<CompanyRole> getRole(long memberDiscordId) {
        return this.roles.stream()
                .filter(companyRole -> companyRole.getMemberDiscordIds().contains(memberDiscordId))
                .findAny();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDiscordServerId() {
        return this.discordServerId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOwnerDiscordId() {
        return this.ownerDiscordId;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Long> getMemberDiscordIds() {
        return this.memberDiscordIds;
    }

    public List<CompanyRole> getRoles() {
        return this.roles;
    }

    public String getDefaultRole() {
        return this.defaultRole;
    }

    public int getMemberLimit() {
        return this.memberLimit;
    }

    public void setMemberLimit(int memberLimit) {
        this.memberLimit = memberLimit;
    }

    public List<Integer> getPurchasedItems() {
        return this.purchasedItems;
    }

}
