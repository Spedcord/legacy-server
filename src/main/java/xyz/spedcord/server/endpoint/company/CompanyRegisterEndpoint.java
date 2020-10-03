package xyz.spedcord.server.endpoint.company;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.company.CompanyRole;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.statistics.Statistics;
import xyz.spedcord.server.statistics.StatisticsController;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * Handles company registration
 *
 * @author Maximilian Dorn
 * @version 2.1.9
 * @since 1.0.0
 */
public class CompanyRegisterEndpoint extends RestrictedEndpoint {

    private final CompanyController companyController;
    private final UserController userController;
    private final StatisticsController statsController;

    public CompanyRegisterEndpoint(CompanyController companyController, UserController userController, StatisticsController statsController) {
        this.companyController = companyController;
        this.userController = userController;
        this.statsController = statsController;
    }

    @Override
    protected void handleFurther(Context context) {
        // Try to get company from request body
        Company company;
        try {
            JsonObject jsonObject = JsonParser.parseString(context.body()).getAsJsonObject();
            jsonObject.addProperty("id", -1);
            jsonObject.add("roles", SpedcordServer.GSON.toJsonTree(CompanyRole.createDefaults()));
            jsonObject.addProperty("defaultRole", "Driver");
            jsonObject.addProperty("memberLimit", Company.DEFAULT_MEMBER_LIMIT);
            company = SpedcordServer.GSON.fromJson(jsonObject, Company.class);
        } catch (Exception ignored) {
            Responses.error("Invalid request body").respondTo(context);
            return;
        }

        // Abort if invalid
        if (company.getName() == null || company.getMemberDiscordIds() == null) {
            Responses.error("Invalid request body").respondTo(context);
            return;
        }

        // Abort if name has invalid length
        if (company.getName().length() >= 24 || company.getName().length() <= 4) {
            Responses.error("The name has an invalid length (4 - 24 chars)").respondTo(context);
            return;
        }

        // Get company owner
        long ownerDiscordId = company.getOwnerDiscordId();
        Optional<User> optional = this.userController.getUser(ownerDiscordId);
        if (optional.isEmpty()) {
            Responses.error("Owner is not registered",
                    "Hint: Did the owner register their Discord account?").respondTo(context);
            return;
        }

        // Abort if owner is in another company
        User user = optional.get();
        if (user.getCompanyId() != -1) {
            Responses.error("The owner is already in a company").respondTo(context);
            return;
        }

        // Add user to first role with admin perms
        company.getRoles().stream()
                .filter(companyRole -> companyRole.hasPermission(CompanyRole.Permission.ADMINISTRATOR))
                .findFirst().ifPresent(companyRole -> companyRole.getMemberDiscordIds().add(ownerDiscordId));

        // Create company and update user
        this.companyController.createCompany(company);
        user.setCompanyId(company.getId());
        this.userController.updateUser(user);

        // Update stats
        Statistics statistics = this.statsController.getStatistics();
        statistics.setTotalCompanies(statistics.getTotalCompanies() + 1);
        this.statsController.update();

        Responses.success("Company was registered").respondTo(context);
    }

}
