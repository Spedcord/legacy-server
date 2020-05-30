package xyz.spedcord.server.endpoint.user;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import xyz.spedcord.server.endpoint.Endpoint;
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
        Optional<Long> paramOptional = getPathParamAsLong("discordId", context);
        if(paramOptional.isEmpty()) {
            Responses.error("Invalid discordId param").respondTo(context);
            return;
        }
        long discordId = paramOptional.get();

        Optional<User> optional = userController.getUser(discordId);
        if(!optional.isPresent()) {
            Responses.error("Unknown user").respondTo(context);
            return;
        }

        JsonObject jsonObj = new Gson().toJsonTree(optional.get()).getAsJsonObject();
        jsonObj.remove("key");
        jsonObj.remove("jobList");

        context.result(jsonObj.toString()).status(200);
    }
}
