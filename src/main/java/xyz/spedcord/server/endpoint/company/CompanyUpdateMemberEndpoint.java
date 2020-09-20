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

public class CompanyUpdateMemberEndpoint extends Endpoint {

    private final CompanyController companyController;
    private final UserController userController;

    public CompanyUpdateMemberEndpoint(CompanyController companyController, UserController userController) {
        this.companyController = companyController;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        Optional<Long> companyDiscordIdOptional = getQueryParamAsLong("companyDiscordId", context);
        if (companyDiscordIdOptional.isEmpty()) {
            Responses.error("Invalid companyDiscordId param").respondTo(context);
            return;
        }

        Optional<Company> companyOptional = companyController.getCompany(companyDiscordIdOptional.get());
        if (companyOptional.isEmpty()) {
            Responses.error("Company does not exist").respondTo(context);
            return;
        }

        Optional<User> changerOptional = getUserFromQuery("changerDiscordId", !RestrictedEndpoint.isAuthorized(context), context, userController);
        if (changerOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }

        Optional<User> userOptional = getUserFromQuery("userDiscordId", false, context, userController);
        if (userOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }

        Optional<String> roleNameOptional = getQueryParam("role", context);
        if (roleNameOptional.isEmpty()) {
            Responses.error("Invalid role param").respondTo(context);
            return;
        }

        User changer = changerOptional.get();
        User user = userOptional.get();
        Company company = companyOptional.get();

        if (company.getId() != changer.getCompanyId()) {
            Responses.error("Changer is not a member of the company").respondTo(context);
            return;
        }

        if (!company.hasPermission(changer.getDiscordId(), CompanyRole.Permission.MANAGE_MEMBERS)
                || (company.hasPermission(user.getDiscordId(), CompanyRole.Permission.ADMINISTRATOR)
                && !company.hasPermission(changer.getDiscordId(), CompanyRole.Permission.ADMINISTRATOR))) {
            Responses.error("Insufficient permissions").respondTo(context);
            return;
        }

        if (!company.getMemberDiscordIds().contains(user.getDiscordId())) {
            Responses.error("User is not a member of the company").respondTo(context);
            return;
        }

        Optional<CompanyRole> roleOptional = company.getRoles().stream()
                .filter(companyRole -> companyRole.getName().equals(roleNameOptional.get()))
                .findAny();
        if (roleOptional.isEmpty()) {
            Responses.error("Unknown role").respondTo(context);
            return;
        }

        CompanyRole companyRole = roleOptional.get();
        if (companyRole.hasPermission(CompanyRole.Permission.ADMINISTRATOR)
                && !company.hasPermission(changer.getDiscordId(), CompanyRole.Permission.ADMINISTRATOR)) {
            Responses.error("Insufficient permissions").respondTo(context);
            return;
        }

        company.getRole(user.getDiscordId()).ifPresent(_role ->
                _role.getMemberDiscordIds().remove(user.getDiscordId()));
        companyRole.getMemberDiscordIds().add(user.getDiscordId());
        companyController.updateCompany(company);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("company", company.getId());
        jsonObject.addProperty("role", companyRole.getName());
        WebhookUtil.callWebhooks(user.getDiscordId(), jsonObject, "USER_ROLE_UPDATE");

        Responses.success("Member role was updated").respondTo(context);
    }
}
