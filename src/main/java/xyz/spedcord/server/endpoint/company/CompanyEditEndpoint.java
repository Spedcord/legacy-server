package xyz.spedcord.server.endpoint.company;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.company.CompanyRole;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

/**
 * Handles company edits
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class CompanyEditEndpoint extends Endpoint {

    private final UserController userController;
    private final CompanyController companyController;

    public CompanyEditEndpoint(UserController userController, CompanyController companyController) {
        this.userController = userController;
        this.companyController = companyController;
    }

    @Override
    public void handle(Context context) {
        // Get company id
        Optional<Integer> companyIdOptional = this.getQueryParamAsInt("companyId", context);
        if (companyIdOptional.isEmpty()) {
            Responses.error("Invalid companyId param").respondTo(context);
            return;
        }

        // Get company
        Optional<Company> companyOptional = this.companyController.getCompany(companyIdOptional.get());
        if (companyOptional.isEmpty()) {
            Responses.error("Company does not exist").respondTo(context);
            return;
        }

        // Get user
        Optional<User> userOptional = this.getUserFromQuery("userDiscordId", !RestrictedEndpoint.isAuthorized(context), context, this.userController);
        if (userOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }

        // Parse body
        String bodyStr = context.body();
        JsonObject body;
        try {
            body = JsonParser.parseString(bodyStr).getAsJsonObject();
            if (body.size() == 0 || (!body.has("name") && !body.has("defaultRole"))) {
                throw new IllegalStateException();
            }
        } catch (Exception ignored) {
            Responses.error("Invalid body").respondTo(context);
            return;
        }

        User user = userOptional.get();
        Company company = companyOptional.get();

        // Abort if user is not member of company
        if (user.getCompanyId() != company.getId()) {
            Responses.error("User is not a member of the company").respondTo(context);
            return;
        }

        // Abort if user has insufficient perms
        if (!company.hasPermission(user.getDiscordId(), CompanyRole.Permission.EDIT_COMPANY)) {
            Responses.error("Insufficient permissions").respondTo(context);
            return;
        }

        if (body.has("name")) {
            // Update name
            String name = body.get("name").getAsString();

            if (name.length() >= 24 || name.length() <= 4) {
                Responses.error("The name has an invalid length (4 - 24 chars)").respondTo(context);
                return;
            }
            company.setName(name);
        }
        if (body.has("defaultRole")) {
            // Update default role
            String defaultRole = body.get("defaultRole").getAsString();

            if (!company.changeDefaultRole(defaultRole)) {
                Responses.error("Unknown role").respondTo(context);
                return;
            }
        }

        // Update company
        this.companyController.updateCompany(company);
        Responses.success("Company was updated").respondTo(context);
    }

}
