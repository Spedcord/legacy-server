package xyz.spedcord.server.endpoint.company;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.company.CompanyRole;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class CompanyRegisterEndpoint extends RestrictedEndpoint {

    private final CompanyController companyController;
    private final UserController userController;

    public CompanyRegisterEndpoint(CompanyController companyController, UserController userController) {
        this.companyController = companyController;
        this.userController = userController;
    }

    @Override
    protected void handleFurther(Context context) {
        Company company;
        try {
            JsonObject jsonObject = JsonParser.parseString(context.body()).getAsJsonObject();
            jsonObject.addProperty("id", -1);
            JsonArray arr = new JsonArray();
            arr.add(SpedcordServer.GSON.toJsonTree(CompanyRole.createDefault()));
            jsonObject.add("roles", arr);
            jsonObject.addProperty("defaultRole", "Default");
            company = SpedcordServer.GSON.fromJson(jsonObject, Company.class);
        } catch (Exception ignored) {
            Responses.error("Invalid request body").respondTo(context);
            return;
        }

        if(company.getName() == null || company.getMemberDiscordIds() == null) {
            Responses.error("Invalid request body").respondTo(context);
            return;
        }

        long ownerDiscordId = company.getOwnerDiscordId();
        Optional<User> optional = userController.getUser(ownerDiscordId);
        if(optional.isEmpty()) {
            Responses.error("Owner is not registered",
                    "Hint: Did the owner register their Discord account?").respondTo(context);
            return;
        }

        User user = optional.get();
        if(user.getCompanyId() != -1) {
            Responses.error("The owner is already in a company").respondTo(context);
            return;
        }

        companyController.createCompany(company);
        user.setCompanyId(company.getId());
        userController.updateUser(user);

        Responses.success("Company was registered").respondTo(context);
    }
}
