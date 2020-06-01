package xyz.spedcord.server.endpoint.user;

import bell.oauth.discord.main.OAuthBuilder;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import xyz.spedcord.common.config.Config;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class UserInfoEndpoint extends Endpoint {

    private final Config config;
    private final UserController userController;

    public UserInfoEndpoint(Config config, UserController userController) {
        this.config = config;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        Optional<Long> paramOptional = getPathParamAsLong("discordId", context);
        if (paramOptional.isEmpty()) {
            Responses.error("Invalid discordId param").respondTo(context);
            return;
        }
        long discordId = paramOptional.get();

        Optional<User> optional = userController.getUser(discordId);
        if (!optional.isPresent()) {
            Responses.error("Unknown user").respondTo(context);
            return;
        }

        User user = optional.get();

        JsonObject jsonObj = SpedcordServer.GSON.toJsonTree(user).getAsJsonObject();
        jsonObj.remove("key");
        jsonObj.remove("jobList");
        jsonObj.remove("accessToken");
        jsonObj.remove("refreshToken");
        jsonObj.remove("tokenExpires");

        OAuthBuilder oAuthBuilder = new OAuthBuilder(
                config.get("oauth-clientid"),
                config.get("oauth-clientsecret"),
                user.getAccessToken(),
                user.getRefreshToken()
        ).setRedirectURI("https://api.spedcord.xyz/user/register/discord");

        JsonObject oAuthObj = new JsonObject();
        try {
            bell.oauth.discord.domain.User discordUser;
            if (user.getTokenExpires() <= System.currentTimeMillis()) {
                oAuthBuilder.refresh();
                discordUser = oAuthBuilder.getUser();

                user.setAccessToken(oAuthBuilder.getAccess_token());
                user.setRefreshToken(oAuthBuilder.getRefresh_token());
                user.setTokenExpires(System.currentTimeMillis() + (oAuthBuilder.getTokenExpiresIn() * 1000));
                userController.updateUser(user);
            } else {
                try {
                    discordUser = oAuthBuilder.getUser();
                } catch (Exception ex) {
                    oAuthBuilder.refresh();
                    discordUser = oAuthBuilder.getUser();

                    user.setAccessToken(oAuthBuilder.getAccess_token());
                    user.setRefreshToken(oAuthBuilder.getRefresh_token());
                    userController.updateUser(user);
                }
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
