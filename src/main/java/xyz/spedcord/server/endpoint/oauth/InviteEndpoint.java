package xyz.spedcord.server.endpoint.oauth;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.oauth.invite.InviteAuthController;
import xyz.spedcord.server.response.Responses;

/**
 * Handles company invites
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class InviteEndpoint extends Endpoint {

    private final InviteAuthController auth;
    private final JoinLinkController joinLinkController;

    public InviteEndpoint(InviteAuthController auth, JoinLinkController joinLinkController) {
        this.auth = auth;
        this.joinLinkController = joinLinkController;
    }

    @Override
    public void handle(Context context) {
        // Get join link id
        String id = context.pathParam("id");

        // Get company id
        int companyId = this.joinLinkController.getCompanyId(id);
        if (companyId == -1) {
            Responses.error("Invalid id param").respondTo(context);
            return;
        }

        // Redirect to auth url
        context.redirect(this.auth.getNewAuthLink(companyId, id));
    }

}
