package xyz.spedcord.server.endpoint.company;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.company.CompanyRole;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;
import xyz.spedcord.server.util.WebhookUtil;

import java.util.Optional;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class CompanyKickMemberEndpoint extends Endpoint {

    private final CompanyController companyController;
    private final UserController userController;

    public CompanyKickMemberEndpoint(CompanyController companyController, UserController userController) {
        this.companyController = companyController;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        Optional<Long> companyDiscordIdOptional = this.getQueryParamAsLong("companyDiscordId", context);
        if (companyDiscordIdOptional.isEmpty()) {
            Responses.error("Invalid companyDiscordId param").respondTo(context);
            return;
        }

        Optional<Company> companyOptional = this.companyController.getCompany(companyDiscordIdOptional.get());
        if (companyOptional.isEmpty()) {
            Responses.error("Company does not exist").respondTo(context);
            return;
        }

        Optional<User> kickerOptional = this.getUserFromQuery("kickerDiscordId", !RestrictedEndpoint.isAuthorized(context), context, this.userController);
        if (kickerOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }

        Optional<User> userToBeKickedOptional = this.getUserFromQuery("userDiscordId", false, context, this.userController);
        if (userToBeKickedOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }

        User kicker = kickerOptional.get();
        User user = userToBeKickedOptional.get();
        Company company = companyOptional.get();

        if (company.getId() != kicker.getCompanyId()) {
            Responses.error("Kicker is not a member of the company").respondTo(context);
            return;
        }

        if (!company.hasPermission(kicker.getDiscordId(), CompanyRole.Permission.MANAGE_MEMBERS)) {
            Responses.error("Insufficient permissions").respondTo(context);
            return;
        }

        if (!company.getMemberDiscordIds().contains(user.getDiscordId())) {
            Responses.error("User is not a member of the company").respondTo(context);
            return;
        }

        company.getRoles().stream()
                .filter(companyRole -> companyRole.getMemberDiscordIds().contains(user.getDiscordId()))
                .forEach(companyRole -> companyRole.getMemberDiscordIds().remove(user.getDiscordId()));

        company.getMemberDiscordIds().remove(user.getDiscordId());
        user.setCompanyId(-1);

        this.companyController.updateCompany(company);
        this.userController.updateUser(user);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("company", company.getId());
        WebhookUtil.callWebhooks(user.getDiscordId(), jsonObject, "USER_LEAVE_COMPANY");

        Responses.success("User was kicked from the company").respondTo(context);
    }
}
