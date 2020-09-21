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
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;
import xyz.spedcord.server.util.WebhookUtil;

import java.util.Optional;

/**
 * Handles the Discord callback for registrations
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class InviteDiscordEndpoint extends Endpoint {

    private final InviteAuthController auth;
    private final JoinLinkController joinLinkController;
    private final UserController userController;
    private final CompanyController companyController;

    public InviteDiscordEndpoint(InviteAuthController auth, JoinLinkController joinLinkController,
                                 UserController userController, CompanyController companyController) {
        this.auth = auth;
        this.joinLinkController = joinLinkController;
        this.userController = userController;
        this.companyController = companyController;
    }

    @Override
    public void handle(Context context) {
        // Get the Discord code
        String code = context.queryParam("code");
        if (code == null) {
            //context.status(400);
            context.redirect("https://www.spedcord.xyz/error/invite/1");
            return;
        }

        // Get the unique state
        String state = context.queryParam("state");
        if (state == null) {
            //context.status(400);
            context.redirect("https://www.spedcord.xyz/error/invite/1");
            return;
        }

        // Exchange code for result
        InviteAuthResult inviteAuthResult = this.auth.exchangeCode(code, state);
        if (inviteAuthResult.getResponse() == Response.ERROR) {
            //Responses.error("Failed").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/invite/1");
            return;
        }

        // Abort if user is a bot
        if (inviteAuthResult.getUser().isBot()) {
            //Responses.error(HttpStatus.FORBIDDEN_403, "You're a bot o.0").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/invite/2");
            return;
        }

        // Get company
        Optional<Company> optional = this.companyController.getCompany(inviteAuthResult.getCompanyId());
        if (optional.isEmpty()) {
            context.status(500);
            return;
        }
        Company company = optional.get();

        // Get internal user
        long userDiscordId = Long.parseLong(inviteAuthResult.getUser().getId());
        Optional<User> userOptional = this.userController.getUser(userDiscordId);
        if (userOptional.isEmpty()) {
            //Responses.error("User is not registered").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/invite/4");
            return;
        }
        User user = userOptional.get();

        // Abort if user is member of the company
        if (company.getMemberDiscordIds().contains(user.getDiscordId())
                || company.getOwnerDiscordId() == user.getDiscordId()) {
            //Responses.error(HttpStatus.FORBIDDEN_403, "You're already a member of this company").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/invite/3");
            return;
        }

        // If user is member of another company leave company first
        if (user.getCompanyId() != -1) {
            optional = this.companyController.getCompany(user.getCompanyId());
            if (optional.isPresent()) {
                Company oldCompany = optional.get();
                oldCompany.getMemberDiscordIds().remove(user.getDiscordId());
                this.companyController.updateCompany(oldCompany);
            }
        }

        // Update user
        user.setCompanyId(company.getId());
        this.userController.updateUser(user);

        // Update company
        company.getMemberDiscordIds().add(user.getDiscordId());
        company.getRoles().stream()
                .filter(companyRole -> companyRole.getName().equals(company.getDefaultRole()))
                .findAny().ifPresent(companyRole -> companyRole.getMemberDiscordIds().add(user.getDiscordId()));
        this.companyController.updateCompany(company);

        // Update join link
        this.joinLinkController.joinLinkUsed(inviteAuthResult.getJoinId());

        // Notify webhooks
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("company", company.getId());
        WebhookUtil.callWebhooks(user.getDiscordId(), jsonObject, "USER_JOIN_COMPANY");

        //Responses.success("You successfully joined the company").respondTo(context);
        context.redirect("https://www.spedcord.xyz/success/invite/1");
    }
}
