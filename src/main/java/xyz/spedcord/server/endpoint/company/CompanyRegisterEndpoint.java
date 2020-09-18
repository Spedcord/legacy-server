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
        Company company;
        try {
            JsonObject jsonObject = JsonParser.parseString(context.body()).getAsJsonObject();
            jsonObject.addProperty("id", -1);
            jsonObject.add("roles", SpedcordServer.GSON.toJsonTree(CompanyRole.createDefaults()));
            jsonObject.addProperty("defaultRole", "Driver");
            company = SpedcordServer.GSON.fromJson(jsonObject, Company.class);
        } catch (Exception ignored) {
            Responses.error("Invalid request body").respondTo(context);
            return;
        }

        if (company.getName() == null || company.getMemberDiscordIds() == null) {
            Responses.error("Invalid request body").respondTo(context);
            return;
        }

        long ownerDiscordId = company.getOwnerDiscordId();
        Optional<User> optional = userController.getUser(ownerDiscordId);
        if (optional.isEmpty()) {
            Responses.error("Owner is not registered",
                    "Hint: Did the owner register their Discord account?").respondTo(context);
            return;
        }

        User user = optional.get();
        if (user.getCompanyId() != -1) {
            Responses.error("The owner is already in a company").respondTo(context);
            return;
        }

        company.getRoles().stream()
                .filter(companyRole -> companyRole.getName().equals("Owner"))
                .findAny().ifPresent(companyRole ->
                companyRole.getMemberDiscordIds().add(ownerDiscordId));

        companyController.createCompany(company);
        user.setCompanyId(company.getId());
        userController.updateUser(user);

        Statistics statistics = statsController.getStatistics();
        statistics.setTotalCompanies(statistics.getTotalCompanies() + 1);
        statsController.update();

        Responses.success("Company was registered").respondTo(context);
    }
}
