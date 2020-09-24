package xyz.spedcord.server.endpoint.user;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.Flag;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;
import xyz.spedcord.server.util.WebhookUtil;

import java.awt.*;
import java.util.Optional;

/**
 * (Un)Flags a user
 *
 * @author Maximilian Dorn
 * @version 2.1.7
 * @since 1.0.0
 */
public class UserFlagEndpoint extends Endpoint {

    private final UserController userController;

    public UserFlagEndpoint(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        Optional<String> flagOptional = this.getQueryParam("flag", context);
        if (flagOptional.isEmpty()) {
            Responses.error("Invalid flag param").respondTo(context);
            return;
        }

        Flag flag;
        try {
            flag = Flag.valueOf(flagOptional.get());
        } catch (Exception ignored) {
            Responses.error("Invalid flag param").respondTo(context);
            return;
        }

        // Get user Discord id
        Optional<Long> userIdOptional = this.getQueryParamAsLong("userId", context);
        if (userIdOptional.isEmpty()) {
            Responses.error("Invalid userId param").respondTo(context);
            return;
        }

        // Get mod user
        Optional<User> optional = this.getUserFromQuery("modId", true, context, this.userController);
        if (optional.isEmpty()) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }
        User mod = optional.get();

        // Abort if users permission level is insufficient
        if (mod.getAccountType().getVal() < flag.getPermissionLevel().getVal()) {
            Responses.error("Insufficient permissions").respondTo(context);
            return;
        }

        // Get user
        long userId = userIdOptional.get();
        Optional<User> userOptional = this.userController.getUser(userId);
        if (userOptional.isEmpty()) {
            Responses.error("Unknown user").respondTo(context);
            return;
        }

        // Set flag and update
        User user = userOptional.get();
        boolean del = false;
        if (user.getFlags().contains(flag)) {
            user.getFlags().remove(flag);
            del = true;
        } else {
            user.getFlags().add(flag);
        }
        this.userController.updateUser(user);

        Responses.success("User was " + (del ? "un" : "") + "flagged").respondTo(context);

        JsonObject data = new JsonObject();
        data.addProperty("msg", "User flag update for user #"
                + user.getId() + ": flag=" + flag.name() + ", remove=" + del);
        data.addProperty("color", new Color(127, 180, 233).getRGB());
        WebhookUtil.callWebhooks(mod.getDiscordId(), data, "MOD_LOG");
    }

}
