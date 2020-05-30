package xyz.spedcord.server.endpoint.user;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class UserChangekeyEndpoint extends RestrictedEndpoint {

    private final UserController userController;

    public UserChangekeyEndpoint(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handleFurther(Context context) {
        String rawDiscordId = context.queryParam("discordId");
        if (rawDiscordId == null) {
            Responses.error("Missing discordId param").respondTo(context);
            return;
        }
        long discordId;
        try {
            discordId = Long.parseLong(rawDiscordId);
        } catch (NumberFormatException ignored) {
            Responses.error("Invalid discordId param").respondTo(context);
            return;
        }

        Optional<User> optional = userController.getUser(discordId);
        if(!optional.isPresent()) {
            Responses.error("Unknown user").respondTo(context);
            return;
        }

        User user = optional.get();
        userController.changeKey(user);
        userController.updateUser(user);

        Responses.success("Key was regenerated").respondTo(context);
    }
}
