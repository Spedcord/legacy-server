package xyz.spedcord.server.endpoint.oauth;

import bell.oauth.discord.main.Response;
import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.oauth.register.RegisterAuthController;
import xyz.spedcord.server.oauth.register.RegisterAuthResult;
import xyz.spedcord.server.statistics.Statistics;
import xyz.spedcord.server.statistics.StatisticsController;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class RegisterDiscordEndpoint extends Endpoint {

    private final String botToken;
    private final RegisterAuthController authController;
    private final UserController userController;
    private final StatisticsController statsController;

    public RegisterDiscordEndpoint(String botToken, RegisterAuthController authController, UserController userController, StatisticsController statsController) {
        this.botToken = botToken;
        this.authController = authController;
        this.userController = userController;
        this.statsController = statsController;
    }

    @Override
    public void handle(Context context) {
        String code = context.queryParam("code");
        if (code == null) {
            //context.status(400);
            context.redirect("https://www.spedcord.xyz/error/user/1");
            return;
        }

        String state = context.queryParam("state");
        if (state == null) {
            //context.status(400);
            context.redirect("https://www.spedcord.xyz/error/user/1");
            return;
        }

        RegisterAuthResult authResult = this.authController.exchangeCode(code, state);
        if (authResult.getResponse() == Response.ERROR) {
            //Responses.error("Failed").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/user/1");
            return;
        }
        if (authResult.getUser().isBot()) {
            //Responses.error(HttpStatus.FORBIDDEN_403, "You're a bot o.0").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/user/2");
            return;
        }

        Optional<User> optional = this.userController.getUser(Long.parseLong(authResult.getUser().getId()));
        if (optional.isPresent()) {
            //Responses.error("This Discord account is already registered").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/user/3");
            return;
        }

        long tokenExpires = System.currentTimeMillis() + (authResult.getTokenExpires() * 1000);
        this.userController.createUser(Long.parseLong(authResult.getUser().getId()), authResult.getAccessToken(), authResult.getRefreshToken(), tokenExpires);

        this.joinGuild(authResult.getUser(), authResult.getAccessToken());

        Statistics statistics = this.statsController.getStatistics();
        statistics.setTotalRegistrations(statistics.getTotalRegistrations() + 1);
        this.statsController.update();

        //Responses.success("Your Discord account was successfully registered").respondTo(context);
        context.redirect("https://www.spedcord.xyz/success/user/1");
    }

    private void joinGuild(bell.oauth.discord.domain.User user, String accessToken) {
        long guildId = 696372817989599412L;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format("https://discord.com/api/guilds/%d/members/%s", guildId, user.getId())).openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Spedcord Server");
            connection.setRequestProperty("Authorization", "Bot " + this.botToken);

            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(String.format("{\"access_token\":\"%s\"}", accessToken).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            connection.getResponseCode();
        } catch (IOException ignored) {
        }
    }

}
