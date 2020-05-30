package xyz.spedcord.server.endpoint.oauth;

import com.google.gson.Gson;
import dev.lukaesebrot.jal.endpoints.Endpoint;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.oauth.AuthResult;
import xyz.spedcord.server.oauth.DiscordAuthorizationReceiver;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class DiscordEndpoint extends Endpoint {

    private DiscordAuthorizationReceiver auth;
    private JoinLinkController joinLinkController;
    private UserController userController;
    private CompanyController companyController;

    public DiscordEndpoint(DiscordAuthorizationReceiver auth, JoinLinkController joinLinkController,
                           UserController userController, CompanyController companyController) {
        this.auth = auth;
        this.joinLinkController = joinLinkController;
        this.userController = userController;
        this.companyController = companyController;
    }

    @Override
    public void handle(Context context) {
        String code = context.queryParam("code");
        if (code == null) {
            context.status(400);
            return;
        }

        String state = context.queryParam("state");
        if (state == null) {
            context.status(400);
            return;
        }

        AuthResult authResult = auth.exchangeCode(code, state);
        if (authResult.getUser().isBot()) {
            Responses.error(HttpStatus.FORBIDDEN_403, "You're a bot o.0");
            return;
        }

        joinLinkController.joinLinkUsed(authResult.getJoinId());

        Optional<Company> optional = companyController.getCompany(authResult.getCompanyId());
        if (optional.isEmpty()) {
            context.status(500);
            return;
        }
        Company company = optional.get();

        long userDiscordId = Long.parseLong(authResult.getUser().getId());
        Optional<User> userOptional = userController.getUser(userDiscordId);
        if (userOptional.isEmpty()) {
            userController.createUser(userDiscordId);
            userOptional = userController.getUser(userDiscordId);
        }
        User user = userOptional.get();
        user.setCompanyId(company.getId());
        userController.updateUser(user);

        company.getMemberDiscordIds().add(user.getDiscordId());
        companyController.updateCompany(company);

        context.result(new Gson().toJson(authResult)).status(200);
    }
}
