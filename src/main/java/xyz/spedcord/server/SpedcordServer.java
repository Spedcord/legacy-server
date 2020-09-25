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
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.company.shop.CompanyShop;
import xyz.spedcord.server.endpoint.company.*;
import xyz.spedcord.server.endpoint.company.shop.ShopBuyItemEndpoint;
import xyz.spedcord.server.endpoint.company.shop.ShopListItemsEndpoint;
import xyz.spedcord.server.endpoint.job.*;
import xyz.spedcord.server.endpoint.oauth.InviteDiscordEndpoint;
import xyz.spedcord.server.endpoint.oauth.InviteEndpoint;
import xyz.spedcord.server.endpoint.oauth.RegisterDiscordEndpoint;
import xyz.spedcord.server.endpoint.oauth.RegisterEndpoint;
import xyz.spedcord.server.endpoint.user.*;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.oauth.invite.InviteAuthController;
import xyz.spedcord.server.oauth.register.RegisterAuthController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.statistics.StatisticsController;
import xyz.spedcord.server.task.PayoutTask;
import xyz.spedcord.server.user.UserController;
import xyz.spedcord.server.util.WebhookUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Server main class
 *
 * @author Maximilian Dorn
 * @version 2.1.12
 * @since 1.0.0
 */
public class SpedcordServer {

    public static final boolean DEV = System.getenv("SPEDCORD_DEV") != null
            && System.getenv("SPEDCORD_DEV").equals("true");
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
    private StatisticsController statsController;
    private Config config;

    /**
     * Server start method
     *
     * @throws IOException when config loading fails
     */
    public void start() throws IOException {
        // Load configs
        this.config = new Config(new File("config.cfg"), new String[]{
                "host", "localhost",
                "port", "5670",
                "requests-per-minute", "120",
                "dev-mode", "false",
                "key", "ENTER_A_SECRET_KEY",
                "oauth-clientid", "ENTER_THE_CLIENTID",
                "oauth-clientsecret", "ENTER_THE_CLIENTSECRET",
                "bot-token", "ENTER_THE_BOT_TOKEN"
        });
        Config mongoConfig = new Config(new File("mongo.cfg"), new String[]{
                "host", "localhost",
                "port", "27017",
                "db", "spedcord"
        });

        // Get secret key from config and set the designated fields value
        KEY = this.config.get("key");

        // Load webhooks
        WebhookUtil.loadWebhooks();

        // Initialize MongoDB service
        MongoDBService mongoDBService = new MongoDBService(
                mongoConfig.get("host"),
                Integer.parseInt(mongoConfig.get("port")),
                mongoConfig.get("db")
        );

        // Invite controllers
        this.inviteAuthController = new InviteAuthController(
                this.config.get("oauth-clientid"),
                this.config.get("oauth-clientsecret")
        );
        this.registerAuthController = new RegisterAuthController(
                this.config.get("oauth-clientid"),
                this.config.get("oauth-clientsecret")
        );
        this.joinLinkController = new JoinLinkController(mongoDBService);
        this.userController = new UserController(mongoDBService);
        this.jobController = new JobController(mongoDBService);
        this.companyController = new CompanyController(mongoDBService);
        this.statsController = new StatisticsController(mongoDBService);

        // Create and start the server
        Javalin javalin = Javalin.create().start(this.config.get("host"), Integer.parseInt(this.config.get("port")));
        RateLimiter rateLimiter = new RateLimiter(Integer.parseInt(this.config.get("requests-per-minute")), ctx ->
                Responses.error(HttpStatus.TOO_MANY_REQUESTS_429, "Too many requests").respondTo(ctx));
        HttpServer server = new HttpServer(javalin, rateLimiter);

        // Init company shop
        CompanyShop.init(this.joinLinkController);

        // Register endpoints and start payout task
        this.registerEndpoints(server);
        this.startPayoutTask();
    }

    /**
     * Starts a new payout task
     */
    private void startPayoutTask() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new PayoutTask(this.config, this.companyController, this.userController), 5, 5, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    /**
     * Registers the endpoints
     *
     * @param server The server
     */
    private void registerEndpoints(HttpServer server) {
        // /invite
        server.endpoint("/invite/discord", HandlerType.GET, new InviteDiscordEndpoint(this.inviteAuthController, this.joinLinkController, this.userController, this.companyController));
        server.endpoint("/invite/:id", HandlerType.GET, new InviteEndpoint(this.inviteAuthController, this.joinLinkController));

        // /user
        server.endpoint("/user/register", HandlerType.GET, new RegisterEndpoint(this.registerAuthController));
        server.endpoint("/user/register/discord", HandlerType.GET, new RegisterDiscordEndpoint(this.config.get("bot-token"), this.registerAuthController, this.userController, this.statsController));
        server.endpoint("/user/info/:discordId", HandlerType.GET, new UserInfoEndpoint(this.config, this.userController));
        server.endpoint("/user/get/:discordId", HandlerType.GET, new UserGetEndpoint(this.userController, this.config));
        server.endpoint("/user/jobs/:discordId", HandlerType.GET, new UserJobsEndpoint(this.userController, this.jobController));
        server.endpoint("/user/changekey", HandlerType.POST, new UserChangekeyEndpoint(this.userController));
        server.endpoint("/user/checkauth", HandlerType.POST, new UserCheckAuthEndpoint(this.userController));
        server.endpoint("/user/flag", HandlerType.POST, new UserFlagEndpoint(this.userController));
        server.endpoint("/user/leavecompany", HandlerType.POST, new UserLeaveCompanyEndpoint(this.userController, this.companyController));
        server.endpoint("/user/listmods", HandlerType.GET, new UserListModsEndpoint(this.userController));
        server.endpoint("/user/update", HandlerType.POST, new UserUpdateEndpoint(this.userController));

        // /company
        server.endpoint("/company/info", HandlerType.GET, new CompanyInfoEndpoint(this.companyController, this.userController, this.jobController));
        server.endpoint("/company/register", HandlerType.POST, new CompanyRegisterEndpoint(this.companyController, this.userController, this.statsController));
        server.endpoint("/company/edit", HandlerType.POST, new CompanyEditEndpoint(this.userController, this.companyController));
        server.endpoint("/company/createjoinlink/:companyId", HandlerType.POST, new CreateJoinLinkEndpoint(this.joinLinkController,
                this.config.get("host"), Integer.parseInt(this.config.get("port"))));
        server.endpoint("/company/list/:sortMode", HandlerType.GET, new CompanyListEndpoint(this.companyController, this.userController, this.jobController));
        server.endpoint("/company/role/update", HandlerType.POST, new CompanyUpdateRoleEndpoint(this.companyController, this.userController));
        server.endpoint("/company/member/kick", HandlerType.POST, new CompanyKickMemberEndpoint(this.companyController, this.userController));
        server.endpoint("/company/member/update", HandlerType.POST, new CompanyUpdateMemberEndpoint(this.companyController, this.userController));
        server.endpoint("/company/shop/buy", HandlerType.POST, new ShopBuyItemEndpoint(this.companyController, this.userController));
        server.endpoint("/company/shop/list", HandlerType.GET, new ShopListItemsEndpoint(this.companyController));

        // /job
        server.endpoint("/job/start", HandlerType.POST, new JobStartEndpoint(this.jobController, this.userController));
        server.endpoint("/job/end", HandlerType.POST, new JobEndEndpoint(this.jobController, this.userController, this.companyController, this.statsController));
        server.endpoint("/job/cancel", HandlerType.POST, new JobCancelEndpoint(this.jobController, this.userController));
        server.endpoint("/job/listunverified", HandlerType.GET, new JobListUnverifiedEndpoint(this.jobController, this.userController));
        server.endpoint("/job/verify", HandlerType.POST, new JobVerifyEndpoint(this.jobController, this.userController));
        server.endpoint("/job/pos", HandlerType.POST, new JobPositionEndpoint(this.userController, this.jobController));
    }

}
