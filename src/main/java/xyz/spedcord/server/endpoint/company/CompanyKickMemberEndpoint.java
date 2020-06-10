package xyz.spedcord.server.endpoint.company;

import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class CompanyKickMemberEndpoint extends RestrictedEndpoint {

    private final CompanyController companyController;
    private final UserController userController;

    public CompanyKickMemberEndpoint(CompanyController companyController, UserController userController) {
        this.companyController = companyController;
        this.userController = userController;
    }

    @Override
    protected void handleFurther(Context context) {
        Optional<Long> companyDiscordIdOptional = getQueryParamAsLong("companyDiscordId", context);
        if(companyDiscordIdOptional.isEmpty()) {
            Responses.error("Invalid companyDiscordId param").respondTo(context);
            return;
        }

        Optional<Company> companyOptional = companyController.getCompany(companyDiscordIdOptional.get());
        if(companyOptional.isEmpty()) {
            Responses.error("Company does not exist").respondTo(context);
            return;
        }

        Optional<User> optional = getUserFromPath("userDiscordId", false, context, userController);
        if (optional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }

        User user = optional.get();
        Company company = companyOptional.get();

        if(!company.getMemberDiscordIds().contains(user.getDiscordId())) {
            Responses.error("User is not a member of the company").respondTo(context);
            return;
        }

        company.getMemberDiscordIds().remove(user.getDiscordId());
        user.setCompanyId(-1);

        companyController.updateCompany(company);
        userController.updateUser(user);

        Responses.success("User was kicked from the company").respondTo(context);
    }
}
