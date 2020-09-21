package xyz.spedcord.server.endpoint.user;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;
import xyz.spedcord.server.util.WebhookUtil;

import java.util.Optional;

/**
 * Removes the user from the company
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class UserLeaveCompanyEndpoint extends RestrictedEndpoint {

    private final UserController userController;
    private final CompanyController companyController;

    public UserLeaveCompanyEndpoint(UserController userController, CompanyController companyController) {
        this.userController = userController;
        this.companyController = companyController;
    }

    @Override
    protected void handleFurther(Context context) {
        // Get internal user
        Optional<User> userOptional = this.getUserFromQuery("discordId", false, context, this.userController);
        if (userOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }
        User user = userOptional.get();

        // Abort if user is not in a company
        if (user.getCompanyId() == -1) {
            Responses.error("The provided user is not in a company").respondTo(context);
            return;
        }

        // Get company
        Optional<Company> companyOptional = this.companyController.getCompany(user.getCompanyId());
        if (companyOptional.isEmpty()) {
            Responses.error("Unknown company").respondTo(context);
            return;
        }
        Company company = companyOptional.get();

        // Abort if user is company owner
        if (company.getOwnerDiscordId() == user.getDiscordId()) {
            Responses.error("The company owner cannot leave the company").respondTo(context);
            return;
        }

        // Update user and company
        user.setCompanyId(-1);
        company.getRole(user.getDiscordId()).ifPresent(companyRole ->
                companyRole.getMemberDiscordIds().remove(user.getDiscordId()));
        company.getMemberDiscordIds().remove(user.getDiscordId());

        this.userController.updateUser(user);
        this.companyController.updateCompany(company);

        // Notify webhooks
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("company", company.getId());
        WebhookUtil.callWebhooks(user.getDiscordId(), jsonObject, "USER_LEAVE_COMPANY");

        Responses.success("The user was removed from the company").respondTo(context);
    }
}
