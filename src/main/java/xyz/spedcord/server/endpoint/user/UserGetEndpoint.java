package xyz.spedcord.server.endpoint.user;

import bell.oauth.discord.main.OAuthBuilder;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import xyz.spedcord.common.config.Config;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class UserGetEndpoint extends RestrictedEndpoint {

    private final UserController userController;
    private final Config config;

    public UserGetEndpoint(UserController userController, Config config) {
        this.userController = userController;
        this.config = config;
    }

    @Override
    public void handleFurther(Context context) {
        Optional<User> optional = getUserFromPath("discordId", false, context, userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }
        User user = optional.get();

        JsonObject jsonObj = SpedcordServer.GSON.toJsonTree(user).getAsJsonObject();

        OAuthBuilder oAuthBuilder = new OAuthBuilder(
                config.get("oauth-clientid"),
                config.get("oauth-clientsecret"),
                user.getAccessToken(),
                user.getRefreshToken()
        ).setRedirectURI("https://api.spedcord.xyz/user/register/discord");

        JsonObject oAuthObj = new JsonObject();
        try {
            bell.oauth.discord.domain.User discordUser;
            try {
                discordUser = oAuthBuilder.getUser();
            } catch (Exception ex) {
                oAuthBuilder.refresh();
                discordUser = oAuthBuilder.getUser();

                user.setAccessToken(oAuthBuilder.getAccess_token());
                user.setRefreshToken(oAuthBuilder.getRefresh_token());
                userController.updateUser(user);
            }

            oAuthObj.addProperty("name", discordUser.getUsername());
            oAuthObj.addProperty("discriminator", discordUser.getDiscriminator());
            oAuthObj.addProperty("avatar", discordUser.getAvatar());
        } catch (Exception e) {
            e.printStackTrace();
            oAuthObj.addProperty("error", e.getMessage());
        }
        jsonObj.add("oauth", oAuthObj);

        context.result(jsonObj.toString()).status(200);
    }
}
