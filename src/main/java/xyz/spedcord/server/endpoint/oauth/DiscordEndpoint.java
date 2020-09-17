package xyz.spedcord.server.endpoint.oauth;

import bell.oauth.discord.main.Response;
import com.google.gson.JsonObject;
import io.javalin.http.Context;
import xyz.spedcord.server.company.Company;
import xyz.spedcord.server.company.CompanyController;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.oauth.invite.InviteAuthController;
import xyz.spedcord.server.oauth.invite.InviteAuthResult;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;
import xyz.spedcord.server.util.WebhookUtil;

import java.util.Optional;

public class DiscordEndpoint extends Endpoint {

    private final InviteAuthController auth;
    private final JoinLinkController joinLinkController;
    private final UserController userController;
    private final CompanyController companyController;

    public DiscordEndpoint(InviteAuthController auth, JoinLinkController joinLinkController,
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
            //context.status(400);
            context.redirect("https://www.spedcord.xyz/error/invite/1");
            return;
        }

        String state = context.queryParam("state");
        if (state == null) {
            //context.status(400);
            context.redirect("https://www.spedcord.xyz/error/invite/1");
            return;
        }

        InviteAuthResult inviteAuthResult = auth.exchangeCode(code, state);
        if (inviteAuthResult.getResponse() == Response.ERROR) {
            //Responses.error("Failed").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/invite/1");
            return;
        }
        if (inviteAuthResult.getUser().isBot()) {
            //Responses.error(HttpStatus.FORBIDDEN_403, "You're a bot o.0").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/invite/2");
            return;
        }

        Optional<Company> optional = companyController.getCompany(inviteAuthResult.getCompanyId());
        if (optional.isEmpty()) {
            context.status(500);
            return;
        }
        Company company = optional.get();

        long userDiscordId = Long.parseLong(inviteAuthResult.getUser().getId());
        Optional<User> userOptional = userController.getUser(userDiscordId);
        if (userOptional.isEmpty()) {
            Responses.error("User is not registered").respondTo(context);
            return;
        }
        User user = userOptional.get();

        if (company.getMemberDiscordIds().contains(user.getDiscordId())
                || company.getOwnerDiscordId() == user.getDiscordId()) {
            //Responses.error(HttpStatus.FORBIDDEN_403, "You're already a member of this company").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/invite/3");
            return;
        }

        if (user.getCompanyId() != -1) {
            optional = companyController.getCompany(user.getCompanyId());
            if (optional.isPresent()) {
                Company oldCompany = optional.get();
                oldCompany.getMemberDiscordIds().remove(user.getDiscordId());
                companyController.updateCompany(oldCompany);
            }
        }

        user.setCompanyId(company.getId());
        userController.updateUser(user);

        company.getMemberDiscordIds().add(user.getDiscordId());
        company.getRoles().stream()
                .filter(companyRole -> companyRole.getName().equals(company.getDefaultRole()))
                .findAny().ifPresent(companyRole -> companyRole.getMemberDiscordIds().add(user.getDiscordId()));
        companyController.updateCompany(company);

        joinLinkController.joinLinkUsed(inviteAuthResult.getJoinId());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("company", company.getId());
        WebhookUtil.callWebhooks(user.getDiscordId(), jsonObject, "USER_JOIN_COMPANY");

        //Responses.success("You successfully joined the company").respondTo(context);
        context.redirect("https://www.spedcord.xyz/success/invite/1");
    }
}
