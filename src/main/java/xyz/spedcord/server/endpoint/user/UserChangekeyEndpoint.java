package xyz.spedcord.server.endpoint.user;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * Changes the users secret key
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class UserChangekeyEndpoint extends RestrictedEndpoint {

    private final UserController userController;

    public UserChangekeyEndpoint(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handleFurther(Context context) {
        // Get the internal user
        Optional<User> optional = this.getUserFromQuery("discordId", false, context, this.userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }
        User user = optional.get();

        // Change key and update user
        this.userController.changeKey(user);
        this.userController.updateUser(user);

        Responses.success(user.getKey()).respondTo(context);
    }
}
