package xyz.spedcord.server.user;

/**
 * @author Maximilian Dorn
 * @version 2.1.6
 * @since 1.0.0
 */
public enum Flag {
    CHEATER(User.AccountType.MOD),
    DONOR(User.AccountType.ADMIN),
    EARLY_BIRD(User.AccountType.ADMIN);

    private final User.AccountType permissionLevel;

    Flag(User.AccountType permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public User.AccountType getPermissionLevel() {
        return this.permissionLevel;
    }
}
