package xyz.spedcord.server.endpoint.user;

import bell.oauth.discord.main.OAuthBuilder;
import bell.oauth.discord.main.Response;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import xyz.spedcord.common.config.Config;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class UserInfoEndpoint extends Endpoint {

    private final Config config;
    private final UserController userController;

    public UserInfoEndpoint(Config config, UserController userController) {
        this.config = config;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        Optional<User> optional = this.getUserFromPath("discordId", false, context, this.userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
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
                this.config.get("oauth-clientid"),
                this.config.get("oauth-clientsecret"),
                user.getAccessToken(),
                user.getRefreshToken()
        ).setRedirectURI("https://api.spedcord.xyz/user/register/discord");

        JsonObject oAuthObj = new JsonObject();
        try {
            bell.oauth.discord.domain.User discordUser;
            if (user.getTokenExpires() <= System.currentTimeMillis()) {
                Response refreshResponse = oAuthBuilder.refresh();
                System.out.println(refreshResponse.name());

                discordUser = oAuthBuilder.getUser();

                user.setAccessToken(oAuthBuilder.getAccess_token());
                user.setRefreshToken(oAuthBuilder.getRefresh_token());
                user.setTokenExpires(System.currentTimeMillis() + (oAuthBuilder.getTokenExpiresIn() * 1000));
                this.userController.updateUser(user);
            } else {
                try {
                    discordUser = oAuthBuilder.getUser();
                } catch (Exception ex) {
                    try {
                        oAuthBuilder.refresh();
                        discordUser = oAuthBuilder.getUser();

                        user.setAccessToken(oAuthBuilder.getAccess_token());
                        user.setRefreshToken(oAuthBuilder.getRefresh_token());
                        this.userController.updateUser(user);
                    } catch (Exception exception) {
                        discordUser = null;
                    }
                }
            }

            if (discordUser == null) {
                throw new IllegalStateException("Access denied");
            }

            oAuthObj.addProperty("name", discordUser.getUsername());
            oAuthObj.addProperty("discriminator", discordUser.getDiscriminator());
            oAuthObj.addProperty("avatar", discordUser.getAvatar());
        } catch (Exception e) {
            System.err.println("Failed to access Discord user: " + e.getMessage());
            oAuthObj.addProperty("error", e.getMessage());
        }
        jsonObj.add("oauth", oAuthObj);

        context.result(jsonObj.toString()).status(200);
    }
}
