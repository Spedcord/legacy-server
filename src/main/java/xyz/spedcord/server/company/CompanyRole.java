package xyz.spedcord.server.company;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CompanyRole {

    private final List<Long> memberDiscordIds;
    private String name;
    private double payout;
    private int permissions;

    @BsonCreator
    public CompanyRole(@BsonProperty("name") String name, @BsonProperty("payout") double payout, @BsonProperty("memberDiscordIds") List<Long> memberDiscordIds, @BsonProperty("permissions") int permissions) {
        this.name = name;
        this.payout = payout;
        this.memberDiscordIds = memberDiscordIds;
        this.permissions = permissions;
    }

    public static CompanyRole[] createDefaults() {
        return new CompanyRole[]{
                new CompanyRole("Owner", 5000, new ArrayList<>(), Permission.calculate(Permission.ADMINISTRATOR)),
                new CompanyRole("Manager", 3000, new ArrayList<>(), Permission.calculate(Permission.MANAGE_MEMBERS)),
                new CompanyRole("Driver", 1000, new ArrayList<>(), Permission.calculate())
        };
    }

    public boolean hasPermission(Permission permission) {
        return Permission.hasPermission(permissions, permission);
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

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    public enum Permission {
        ADMINISTRATOR(0x01),
        EDIT_COMPANY(0x02),
        MANAGE_MEMBERS(0x04),
        MANAGE_ROLES(0x08),
        ;

        private final int flag;

        Permission(int flag) {
            this.flag = flag;
        }

        public static boolean hasPermission(int permInt, Permission permission) {
            if(permission != ADMINISTRATOR && hasPermission(permInt, ADMINISTRATOR)) {
                return true;
            }
            return (permInt & permission.flag) == permission.flag;
        }

        public static int calculate(Permission... permissions) {
            int i = 0;
            for (Permission permission : permissions) {
                i |= permission.flag;
            }
            return i;
        }

        public int getFlag() {
            return flag;
        }
    }

}
