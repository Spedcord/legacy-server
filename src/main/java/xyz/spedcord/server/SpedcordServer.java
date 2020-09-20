package xyz.spedcord.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import dev.lukaesebrot.jal.endpoints.HttpServer;
import dev.lukaesebrot.jal.ratelimiting.RateLimiter;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.common.config.Config;
import xyz.spedcord.common.mongodb.MongoDBService;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.company.*;
import xyz.spedcord.server.endpoint.job.*;
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
import xyz.spedcord.server.statistics.StatisticsController;
import xyz.spedcord.server.user.UserController;
import xyz.spedcord.server.util.WebhookUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SpedcordServer {

    public static final boolean DEV = System.getenv("SPEDCORD_DEV") != null
            && System.getenv("SPEDCORD_DEV").equals("true");
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .create();
    public static final long[] MODERATORS = {347018538713874444L, 332142165402714113L};
    public static String KEY = null;

    private InviteAuthController inviteAuthController;
    private RegisterAuthController registerAuthController;
    private JoinLinkController joinLinkController;
    private UserController userController;
    private JobController jobController;
    private CompanyController companyController;
    private StatisticsController statsController;
    private Config config;

    public void start() throws IOException {
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

        KEY = this.config.get("key");

        WebhookUtil.loadWebhooks();

        MongoDBService mongoDBService = new MongoDBService(
                mongoConfig.get("host"),
                Integer.parseInt(mongoConfig.get("port")),
                mongoConfig.get("db")
        );

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

        System.out.println(this.userController.getUsers().size() + " users");
        System.out.println(this.companyController.getCompanies().size() + " companies");

        Javalin app = Javalin.create().start(this.config.get("host"), Integer.parseInt(this.config.get("port")));
        RateLimiter rateLimiter = new RateLimiter(Integer.parseInt(this.config.get("requests-per-minute")), ctx ->
                Responses.error(HttpStatus.TOO_MANY_REQUESTS_429, "Too many requests").respondTo(ctx));
        HttpServer server = new HttpServer(app, rateLimiter);

        this.registerEndpoints(server);
        this.startPayoutTimer();
    }

    private void startPayoutTimer() {
        String lastPayoutStr = this.config.get("lastPayout");
        if (lastPayoutStr == null) {
            lastPayoutStr = "0";
        }
        AtomicLong lastPayout = new AtomicLong(Long.parseLong(lastPayoutStr));

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - lastPayout.get() >= TimeUnit.DAYS.toMillis(7)) {
                lastPayout.set(System.currentTimeMillis());
                this.config.set("lastPayout", lastPayout.get() + "");
                try {
                    this.config.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.companyController.getCompanies().forEach(company -> {
                    if (company.getBalance() <= 0) {
                        JsonObject dataObj = new JsonObject();
                        dataObj.addProperty("company", company.getId());
                        dataObj.addProperty("msg", "The company has a negative balance. Company members can not be paid.");

                        WebhookUtil.callWebhooks(-1, dataObj, "WARN");
                        return;
                    }

                    AtomicReference<Double> totalPayouts = new AtomicReference<>(0D);
                    company.getMemberDiscordIds().forEach(memberId -> {
                        this.userController.getUser(memberId).ifPresent(user -> {
                            double payout = company.getRoles().stream()
                                    .filter(companyRole -> companyRole.getMemberDiscordIds().contains(memberId))
                                    .findAny().get().getPayout();
                            user.setBalance(user.getBalance() + payout);
                            totalPayouts.set(totalPayouts.get() + payout);
                            this.userController.updateUser(user);
                        });
                    });

                    this.userController.getUser(company.getOwnerDiscordId()).ifPresent(user -> {
                        double payout = company.getRoles().stream()
                                .filter(companyRole -> companyRole.getMemberDiscordIds().contains(user.getDiscordId()))
                                .findAny().get().getPayout();
                        user.setBalance(user.getBalance() + payout);
                        totalPayouts.set(totalPayouts.get() + payout);
                        this.userController.updateUser(user);
                    });

                    company.setBalance(company.getBalance() - (totalPayouts.get()));
                    this.companyController.updateCompany(company);
                });
                System.out.println("Payouts were paid");
            }
        }, 5, 5, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    private void registerEndpoints(HttpServer server) {
        server.endpoint("/invite/discord", HandlerType.GET, new DiscordEndpoint(this.inviteAuthController, this.joinLinkController, this.userController, this.companyController));
        server.endpoint("/invite/:id", HandlerType.GET, new InviteEndpoint(this.inviteAuthController, this.joinLinkController));

        server.endpoint("/user/register", HandlerType.GET, new RegisterEndpoint(this.registerAuthController));
        server.endpoint("/user/register/discord", HandlerType.GET, new RegisterDiscordEndpoint(this.config.get("bot-token"), this.registerAuthController, this.userController, this.statsController));
        server.endpoint("/user/info/:discordId", HandlerType.GET, new UserInfoEndpoint(this.config, this.userController));
        server.endpoint("/user/get/:discordId", HandlerType.GET, new UserGetEndpoint(this.userController, this.config));
        server.endpoint("/user/jobs/:discordId", HandlerType.GET, new UserJobsEndpoint(this.userController, this.jobController));
        server.endpoint("/user/changekey", HandlerType.POST, new UserChangekeyEndpoint(this.userController));
        server.endpoint("/user/checkauth", HandlerType.POST, new UserCheckAuthEndpoint(this.userController));
        server.endpoint("/user/cheater", HandlerType.POST, new UserCheaterEndpoint(this.userController));
        server.endpoint("/user/leavecompany", HandlerType.POST, new UserLeaveCompanyEndpoint(this.userController, this.companyController));
        server.endpoint("/user/listmods", HandlerType.GET, new UserListModsEndpoint());

        server.endpoint("/company/info", HandlerType.GET, new CompanyInfoEndpoint(this.companyController, this.userController, this.jobController));
        server.endpoint("/company/register", HandlerType.POST, new CompanyRegisterEndpoint(this.companyController, this.userController, this.statsController));
        server.endpoint("/company/edit", HandlerType.POST, new CompanyEditEndpoint(this.userController, this.companyController));
        server.endpoint("/company/createjoinlink/:companyId", HandlerType.POST, new CreateJoinLinkEndpoint(this.joinLinkController,
                this.config.get("host"), Integer.parseInt(this.config.get("port"))));
        //server.endpoint("/company/shop", HandlerType.POST, new ShopBuyItemEndpoint(companyController, joinLinkController));
        server.endpoint("/company/list/:sortMode", HandlerType.GET, new CompanyListEndpoint(this.companyController, this.userController));
        server.endpoint("/company/role/update", HandlerType.POST, new CompanyUpdateRoleEndpoint(this.companyController, this.userController));
        server.endpoint("/company/member/kick", HandlerType.POST, new CompanyKickMemberEndpoint(this.companyController, this.userController));
        server.endpoint("/company/member/update", HandlerType.POST, new CompanyUpdateMemberEndpoint(this.companyController, this.userController));

        server.endpoint("/job/start", HandlerType.POST, new JobStartEndpoint(this.jobController, this.userController));
        server.endpoint("/job/end", HandlerType.POST, new JobEndEndpoint(this.jobController, this.userController, this.companyController, this.statsController));
        server.endpoint("/job/cancel", HandlerType.POST, new JobCancelEndpoint(this.jobController, this.userController));
        server.endpoint("/job/listunverified", HandlerType.GET, new JobListUnverifiedEndpoint(this.jobController, this.userController));
        server.endpoint("/job/verify", HandlerType.POST, new JobVerifyEndpoint(this.jobController, this.userController));
        server.endpoint("/job/pos", HandlerType.POST, new JobPositionEndpoint(this.userController, this.jobController));
    }

}
