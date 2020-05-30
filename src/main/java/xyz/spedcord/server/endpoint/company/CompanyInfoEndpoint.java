package xyz.spedcord.server.endpoint.company;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.lukaesebrot.jal.endpoints.Endpoint;
import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.job.Job;
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
        String rawDiscordServerId = context.pathParam("discordServerId");
        long discordServerId;
        try {
            discordServerId = Long.parseLong(rawDiscordServerId);
        } catch (NumberFormatException ignored) {
            Responses.error("Invalid discordServerId param").respondTo(context);
            return;
        }

        Optional<Company> optional = companyController.getCompany(discordServerId);
        if(optional.isEmpty()) {
            Responses.error("Invalid discordServerId param").respondTo(context);
            return;
        }

        Gson gson = new Gson();
        Company company = optional.get();
        JsonObject jsonObj = gson.toJsonTree(company).getAsJsonObject();
        JsonObject logbook = new JsonObject();
        for (Long memberDiscordId : company.getMemberDiscordIds()) {
            Optional<User> userOptional = userController.getUser(memberDiscordId);
            if(userOptional.isEmpty()) {
                continue;
            }

            User user = userOptional.get();
            JsonArray array = new JsonArray();
            for (int jobId : user.getJobList()) {
                Job job = jobController.getJob(jobId);
                array.add(gson.toJson(job));
            }
            logbook.add(String.valueOf(user.getDiscordId()), array);
        }
        jsonObj.add("logbook", logbook);

        context.result(gson.toJson(jsonObj)).status(200);
    }
}