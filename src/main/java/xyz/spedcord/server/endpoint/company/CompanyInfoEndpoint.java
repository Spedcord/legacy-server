package xyz.spedcord.server.endpoint.company;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.job.JobController;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class CompanyInfoEndpoint extends Endpoint {

    private final CompanyController companyController;
    private final UserController userController;
    private final JobController jobController;

    public CompanyInfoEndpoint(CompanyController companyController, UserController userController, JobController jobController) {
        this.companyController = companyController;
        this.userController = userController;
        this.jobController = jobController;
    }

    @Override
    public void handle(Context context) {
        Optional<Long> paramOptional = getQueryParamAsLong("discordServerId", context);
        if (paramOptional.isEmpty()) {
            Optional<Integer> idOptional = getQueryParamAsInt("id", context);
            if (idOptional.isPresent()) {
                handleWithId(idOptional.get(), context);
                return;
            }

            Responses.error("Invalid discordServerId param").respondTo(context);
            return;
        }
        long discordServerId = paramOptional.get();

        handleWithDiscordId(discordServerId, context);
    }

    private void handleWithDiscordId(long discordServerId, Context context) {
        Optional<Company> optional = companyController.getCompany(discordServerId);
        if (optional.isEmpty()) {
            Responses.error("Invalid discordServerId param").respondTo(context);
            return;
        }

        Company company = optional.get();
        handleFurther(company, context);
    }

    private void handleWithId(int id, Context context) {
        Optional<Company> optional = companyController.getCompany(id);
        if (optional.isEmpty()) {
            Responses.error("Invalid id param").respondTo(context);
            return;
        }

        Company company = optional.get();
        handleFurther(company, context);
    }

    private void handleFurther(Company company, Context context) {
        JsonObject jsonObj = SpedcordServer.GSON.toJsonTree(company).getAsJsonObject();
        JsonObject logbook = new JsonObject();
        for (Long memberDiscordId : company.getMemberDiscordIds()) {
            Optional<User> userOptional = userController.getUser(memberDiscordId);
            if (userOptional.isEmpty()) {
                continue;
            }

            User user = userOptional.get();
            JsonArray array = SpedcordServer.GSON.toJsonTree(jobController.getJobs(user.getJobList())).getAsJsonArray();
            logbook.add(String.valueOf(user.getDiscordId()), array);
        }
        jsonObj.add("logbook", logbook);

        context.result(SpedcordServer.GSON.toJson(jsonObj)).status(200);
    }

}
