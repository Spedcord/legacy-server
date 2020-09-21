package xyz.spedcord.server.endpoint.user;

import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * Handles user updates
 *
 * @author Maximilian Dorn
 * @version 2.1.0
 * @since 2.1.0
 */
public class UserUpdateEndpoint extends Endpoint {

    private final UserController userController;

    public UserUpdateEndpoint(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        // Get user
        Optional<User> userOptional = this.getUserFromQuery("userId", true, context, this.userController);
        if (userOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }

        // Abort if user is not an admin
        User user = userOptional.get();
        if (user.getAccountType() != User.AccountType.ADMIN) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        // Get other user
        Optional<User> otherUserOptional = this.getUserFromQuery("otherUserId", false, context, this.userController);
        if (otherUserOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }

        // Get account type
        Optional<Integer> accountTypeOptional = this.getQueryParamAsInt("accountType", context);
        if (accountTypeOptional.isEmpty()) {
            Responses.error("Invalid accountType param").respondTo(context);
            return;
        }

        // Abort if account type is invalid
        User.AccountType accountType = User.AccountType.fromVal(accountTypeOptional.get());
        if (accountType == null) {
            Responses.error("Unknown account type").respondTo(context);
            return;
        }

        // Update user
        User otherUser = otherUserOptional.get();
        otherUser.setAccountType(accountType);

        this.userController.updateUser(user);
        Responses.success("User was updates").respondTo(context);
    }

}
