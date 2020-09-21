package xyz.spedcord.server.endpoint.user;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * Checks if the provided credentials are valid
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class UserCheckAuthEndpoint extends Endpoint {

    private final UserController userController;

    public UserCheckAuthEndpoint(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(Context ctx) {
        // Could be made into a one-liner with 'getUserFromQuery("userDiscordId", true, ctx, userController).isEmpty()'
        // That would only give 2 status codes though

        // Get Discord id
        Optional<Long> userDiscordIdOptional = this.getQueryParamAsLong("userDiscordId", ctx);
        if (userDiscordIdOptional.isEmpty()) {
            ctx.status(400);
            return;
        }

        // Get key
        Optional<String> keyOptional = this.getQueryParam("key", ctx);
        if (keyOptional.isEmpty()) {
            ctx.status(400);
            return;
        }

        long userDiscordId = userDiscordIdOptional.get();
        String key = keyOptional.get();

        // Get user
        Optional<User> userOptional = this.userController.getUser(userDiscordId);
        if (userOptional.isEmpty()) {
            ctx.status(404);
            return;
        }

        User user = userOptional.get();
        ctx.status(key.equals(user.getKey()) ? 200 : 401);
    }
}
