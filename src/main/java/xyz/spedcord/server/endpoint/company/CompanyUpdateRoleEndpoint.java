package xyz.spedcord.server.endpoint.company;

import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.company.CompanyRole;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.ArrayList;
import java.util.Optional;

public class CompanyUpdateRoleEndpoint extends Endpoint {

    private final CompanyController companyController;
    private final UserController userController;

    public CompanyUpdateRoleEndpoint(CompanyController companyController, UserController userController) {
        this.companyController = companyController;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        Optional<Integer> modeOptional = this.getQueryParamAsInt("mode", context);
        if (modeOptional.isEmpty()) {
            Responses.error("Invalid mode param").respondTo(context);
            return;
        }

        int mode = modeOptional.get();
        if (mode > 2 || mode < 0) {
            Responses.error("Invalid mode param").respondTo(context);
            return;
        }

        Optional<Integer> companyIdOptional = this.getQueryParamAsInt("companyId", context);
        if (companyIdOptional.isEmpty()) {
            Responses.error("Invalid companyId param").respondTo(context);
            return;
        }

        Optional<Company> companyOptional = this.companyController.getCompany(companyIdOptional.get());
        if (companyOptional.isEmpty()) {
            Responses.error("Company does not exist").respondTo(context);
            return;
        }

        Optional<User> userOptional = this.getUserFromQuery("userDiscordId", !RestrictedEndpoint.isAuthorized(context), context, this.userController);
        if (userOptional.isEmpty()) {
            Responses.error("Unknown user / Invalid request").respondTo(context);
            return;
        }

        if (context.body().equals("")) {
            Responses.error("No body was supplied").respondTo(context);
            return;
        }
        Optional<CompanyRole> companyRoleOptional = this.getCompanyRoleFromBody(context);
        if (companyRoleOptional.isEmpty()) {
            Responses.error("Invalid body").respondTo(context);
            return;
        }

        User user = userOptional.get();
        Company company = companyOptional.get();
        CompanyRole companyRole = companyRoleOptional.get();

        if (user.getCompanyId() != company.getId()) {
            Responses.error("User is not a member of the company").respondTo(context);
            return;
        }

        if (!company.hasPermission(user.getDiscordId(), CompanyRole.Permission.MANAGE_ROLES)) {
            Responses.error("Insufficient permissions").respondTo(context);
            return;
        }

        CompanyRole role;
        switch (mode) {
            case 0:
                //Edit
                role = company.getRoles().stream()
                        .filter(_role -> _role.getName().equals(companyRole.getName()))
                        .findAny().orElse(null);
                if (role == null) {
                    Responses.error("Unknown role").respondTo(context);
                    return;
                }
                if (role.hasPermission(CompanyRole.Permission.ADMINISTRATOR) && !companyRole.hasPermission(CompanyRole.Permission.ADMINISTRATOR)
                        && company.getRoles().stream()
                        .filter(_role -> _role.hasPermission(CompanyRole.Permission.ADMINISTRATOR))
                        .count() == 1) {
                    Responses.error("At least one role with administrator access has to exist").respondTo(context);
                    return;
                }

                role.setPermissions(companyRole.getPermissions());
                role.setPayout(companyRole.getPayout());
                break;
            case 1:
                //Create
                if (company.getRoles().stream()
                        .anyMatch(_role -> _role.getName().equals(companyRole.getName()))) {
                    Responses.error("Role already exists").respondTo(context);
                    return;
                }
                if (companyRole.getName().length() > 24 || companyRole.getName().length() < 4) {
                    Responses.error("Role names have to be longer than 3 chars and shorter than 25 chars").respondTo(context);
                    return;
                }

                company.getRoles().add(new CompanyRole(companyRole.getName(), companyRole.getPayout(), new ArrayList<>(), companyRole.getPermissions()));
                break;
            case 2:
                //Remove
                role = company.getRoles().stream()
                        .filter(_role -> _role.getName().equals(companyRole.getName()))
                        .findAny().orElse(null);
                if (role == null) {
                    Responses.error("Role doesn't exist").respondTo(context);
                    return;
                }
                if (role.getName().equals(company.getDefaultRole())) {
                    Responses.error("Default role cannot be deleted").respondTo(context);
                    return;
                }
                if (role.hasPermission(CompanyRole.Permission.ADMINISTRATOR) && company.getRoles().stream()
                        .filter(_role -> _role.hasPermission(CompanyRole.Permission.ADMINISTRATOR))
                        .count() == 1) {
                    Responses.error("At least one role with administrator access has to exist").respondTo(context);
                    return;
                }

                company.getRoles().remove(role);
                break;
        }

        this.companyController.updateCompany(company);
        Responses.success(mode == 0 ? "Company role was updated" : mode == 1 ? "Company role was created" : "Company role was deleted").respondTo(context);
    }

}
