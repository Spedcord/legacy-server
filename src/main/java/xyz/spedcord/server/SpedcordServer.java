package xyz.spedcord.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import dev.lukaesebrot.jal.endpoints.HttpServer;
import dev.lukaesebrot.jal.ratelimiting.RateLimiter;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.common.config.Config;
import xyz.spedcord.common.sql.MySqlService;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.company.CompanyInfoEndpoint;
import xyz.spedcord.server.endpoint.company.CompanyKickMemberEndpoint;
import xyz.spedcord.server.endpoint.company.CompanyRegisterEndpoint;
import xyz.spedcord.server.endpoint.company.CreateJoinLinkEndpoint;
import xyz.spedcord.server.endpoint.job.JobCancelEndpoint;
import xyz.spedcord.server.endpoint.job.JobEndEndpoint;
import xyz.spedcord.server.endpoint.job.JobStartEndpoint;
import xyz.spedcord.server.endpoint.oauth.DiscordEndpoint;
import xyz.spedcord.server.endpoint.oauth.InviteEndpoint;
import xyz.spedcord.server.endpoint.oauth.RegisterDiscordEndpoint;
import xyz.spedcord.server.endpoint.oauth.RegisterEndpoint;
import xyz.spedcord.server.endpoint.user.*;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.oauth.invite.InviteAuthController;
import xyz.spedcord.server.oauth.register.RegisterAuthController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.UserController;
import xyz.spedcord.server.util.WebhookUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpedcordServer {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .create();
    public static String KEY = null;

    private InviteAuthController inviteAuthController;
    private RegisterAuthController registerAuthController;
    private JoinLinkController joinLinkController;
    private UserController userController;
    private JobController jobController;
    private CompanyController companyController;
    private Config config;

    public void start() throws IOException {
        config = new Config(new File("config.cfg"), new String[]{
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

        WebhookUtil.loadWebhooks();

        MySqlService mySqlService;
        try {
            mySqlService = new MySqlService(mySqlConfig);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        inviteAuthController = new InviteAuthController(
                config.get("oauth-clientid"),
                config.get("oauth-clientsecret")
        );
        registerAuthController = new RegisterAuthController(
                config.get("oauth-clientid"),
                config.get("oauth-clientsecret")
        );
        joinLinkController = new JoinLinkController(mySqlService);
        userController = new UserController(mySqlService);
        jobController = new JobController(mySqlService);
        companyController = new CompanyController(mySqlService);

        Javalin app = Javalin.create().start(config.get("host"), Integer.parseInt(config.get("port")));
        RateLimiter rateLimiter = new RateLimiter(Integer.parseInt(config.get("requests-per-minute")), ctx ->
                Responses.error(HttpStatus.TOO_MANY_REQUESTS_429, "Too many requests").respondTo(ctx));
        HttpServer server = new HttpServer(app, rateLimiter);

        registerEndpoints(server);
        startSqlPinger(mySqlService);
    }

    private void startSqlPinger(MySqlService sqlService) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                sqlService.update("SELECT 1");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, 1, 5, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    private void registerEndpoints(HttpServer server) {
        server.endpoint("/invite/discord", HandlerType.GET, new DiscordEndpoint(inviteAuthController, joinLinkController, userController, companyController));
        server.endpoint("/invite/:id", HandlerType.GET, new InviteEndpoint(inviteAuthController, joinLinkController));

        server.endpoint("/user/register", HandlerType.GET, new RegisterEndpoint(registerAuthController));
        server.endpoint("/user/register/discord", HandlerType.GET, new RegisterDiscordEndpoint(registerAuthController, userController));
        server.endpoint("/user/info/:discordId", HandlerType.GET, new UserInfoEndpoint(config, userController));
        server.endpoint("/user/get/:discordId", HandlerType.GET, new UserGetEndpoint(userController, config));
        server.endpoint("/user/jobs/:discordId", HandlerType.GET, new UserJobsEndpoint(userController, jobController));
        server.endpoint("/user/changekey", HandlerType.POST, new UserChangekeyEndpoint(userController));
        server.endpoint("/user/checkauth", HandlerType.POST, new UserCheckAuthEndpoint(userController));

        server.endpoint("/company/info", HandlerType.GET, new CompanyInfoEndpoint(companyController, userController, jobController));
        server.endpoint("/company/register", HandlerType.POST, new CompanyRegisterEndpoint(companyController, userController));
        server.endpoint("/company/kickmember", HandlerType.POST, new CompanyKickMemberEndpoint(companyController, userController));
        server.endpoint("/company/createjoinlink/:companyId", HandlerType.POST, new CreateJoinLinkEndpoint(joinLinkController,
                config.get("host"), Integer.parseInt(config.get("port"))));

        server.endpoint("/job/start", HandlerType.POST, new JobStartEndpoint(jobController, userController));
        server.endpoint("/job/end", HandlerType.POST, new JobEndEndpoint(jobController, userController));
        server.endpoint("/job/cancel", HandlerType.POST, new JobCancelEndpoint(jobController, userController));
    }

}
