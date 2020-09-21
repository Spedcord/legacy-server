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
import xyz.spedcord.server.user.UserController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Responds with information about the company
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
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
        // Check if server should handle request with company id or Discord server id
        Optional<Long> paramOptional = this.getQueryParamAsLong("discordServerId", context);
        if (paramOptional.isEmpty()) {
            Optional<Integer> idOptional = this.getQueryParamAsInt("id", context);
            if (idOptional.isPresent()) {
                this.handleWithId(idOptional.get(), context);
                return;
            }

            Responses.error("Invalid discordServerId param").respondTo(context);
            return;
        }

        long discordServerId = paramOptional.get();
        this.handleWithDiscordId(discordServerId, context);
    }

    /**
     * Handles the request with a Discord server id
     *
     * @param discordServerId The Discord server id
     * @param context         The context
     */
    private void handleWithDiscordId(long discordServerId, Context context) {
        // Get company
        Optional<Company> optional = this.companyController.getCompany(discordServerId);
        if (optional.isEmpty()) {
            Responses.error("Invalid discordServerId param").respondTo(context);
            return;
        }

        Company company = optional.get();
        this.handleFurther(company, context);
    }

    /**
     * Handle request with company id
     *
     * @param id      The company id
     * @param context The context
     */
    private void handleWithId(int id, Context context) {
        // Get company
        Optional<Company> optional = this.companyController.getCompany(id);
        if (optional.isEmpty()) {
            Responses.error("Invalid id param").respondTo(context);
            return;
        }

        Company company = optional.get();
        this.handleFurther(company, context);
    }

    /**
     * Handles the request after company was fetched
     *
     * @param company The company
     * @param context The context
     */
    private void handleFurther(Company company, Context context) {
        JsonObject jsonObj = SpedcordServer.GSON.toJsonTree(company).getAsJsonObject();

        // Determine company rank
        List<Company> sortedCompanies = this.companyController.getCompanies().stream()
                .sorted(Comparator.comparingDouble(value -> ((Company) value).getBalance()).reversed())
                .collect(Collectors.toList());
        int rank = sortedCompanies.indexOf(company) + 1;
        jsonObj.addProperty("rank", rank);

        // Get jobs
        JsonArray logbook = new JsonArray();
        this.jobController.getJobs(company, this.userController).forEach(job ->
                logbook.add(SpedcordServer.GSON.toJsonTree(job)));
        jsonObj.add("logbook", logbook);

        context.result(SpedcordServer.GSON.toJson(jsonObj)).status(200);
    }

}
