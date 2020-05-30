package xyz.spedcord.server.endpoint.user;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.lukaesebrot.jal.endpoints.Endpoint;
import io.javalin.http.Context;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class UserInfoEndpoint extends Endpoint {

    private final UserController userController;

    public UserInfoEndpoint(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
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

        JsonObject jsonObj = new Gson().toJsonTree(optional.get()).getAsJsonObject();
        jsonObj.remove("key");

        context.result(jsonObj.toString()).status(200);
    }
}
