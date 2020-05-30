package xyz.spedcord.server;

import dev.lukaesebrot.jal.endpoints.HttpServer;
import dev.lukaesebrot.jal.ratelimiting.RateLimiter;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.common.config.Config;
import xyz.spedcord.common.sql.MySqlService;
import xyz.spedcord.server.endpoint.oauth.DiscordEndpoint;
import xyz.spedcord.server.endpoint.oauth.InviteEndpoint;
import xyz.spedcord.server.endpoint.user.UserGetEndpoint;
import xyz.spedcord.server.endpoint.user.UserInfoEndpoint;
import xyz.spedcord.server.oauth.DiscordAuthorizationReceiver;
import xyz.spedcord.server.oauth.JoinLinkRetriever;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.UserController;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SpedcordServer {

    public static String KEY = null;

    public static void main(String[] args) throws IOException {
        Config config = new Config(new File("config.cfg"), new String[]{
                "host", "localhost",
                "port", "5670",
                "requests-per-minute", "120",
                "dev-mode", "false",
                "key", "ENTER_A_SECRET_KEY",
                "oauth-clientid", "ENTER_THE_CLIENTID",
                "oauth-clientsecret", "ENTER_THE_CLIENTSECRET"
        });
        Config mySqlConfig = new Config(new File("mysql.cfg"), new String[]{
                "host", "localhost",
                "user", "",
                "pass", "",
                "db", "spedcord",
                "port", "3306"
        });

        KEY = config.get("key");

        MySqlService mySqlService;
        try {
            mySqlService = new MySqlService(mySqlConfig);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        DiscordAuthorizationReceiver auth = new DiscordAuthorizationReceiver(
                config.get("oauth-clientid"),
                config.get("oauth-clientsecret")
        );
        JoinLinkRetriever joinLinkRetriever = new JoinLinkRetriever(mySqlService);
        UserController userController = new UserController(mySqlService);

        Javalin app = Javalin.create().start(config.get("host"), Integer.parseInt(config.get("port")));
        RateLimiter rateLimiter = new RateLimiter(Integer.parseInt(config.get("requests-per-minute")), ctx ->
                Responses.error(HttpStatus.TOO_MANY_REQUESTS_429, "Too many requests").respondTo(ctx));
        HttpServer server = new HttpServer(app, rateLimiter);

        server.endpoint("/invite", HandlerType.GET, new InviteEndpoint(auth, joinLinkRetriever));
        server.endpoint("/discord", HandlerType.GET, new DiscordEndpoint(auth, joinLinkRetriever)); //TODO
        server.endpoint("/user/info", HandlerType.GET, new UserInfoEndpoint(userController));
        server.endpoint("/user/get", HandlerType.GET, new UserGetEndpoint(userController));
    }

}
