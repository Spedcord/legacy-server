package xyz.spedcord.server.endpoint.user;

import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class UserLeaveCompanyEndpoint extends RestrictedEndpoint {

    private final UserController userController;
    private final CompanyController companyController;

    public UserLeaveCompanyEndpoint(UserController userController, CompanyController companyController) {
        this.userController = userController;
        this.companyController = companyController;
    }

    @Override
    protected void handleFurther(Context context) {
        Optional<User> userOptional = getUserFromQuery("discordId", false, context, userController);
        if(userOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }
        User user = userOptional.get();

        if(user.getCompanyId() == -1) {
            Responses.error("The provided user is not in a company").respondTo(context);
            return;
        }

        Optional<Company> companyOptional = companyController.getCompany(user.getCompanyId());
        if(companyOptional.isEmpty()) {
            Responses.error("Unknown company").respondTo(context);
            return;
        }
        Company company = companyOptional.get();

        user.setCompanyId(-1);
        company.getMemberDiscordIds().remove(user.getDiscordId());

        userController.updateUser(user);
        companyController.updateCompany(company);

        Responses.success("The user was removed from the company").respondTo(context);
    }
}
