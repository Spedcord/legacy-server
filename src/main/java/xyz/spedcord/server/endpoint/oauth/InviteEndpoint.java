package xyz.spedcord.server.endpoint.oauth;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.oauth.invite.InviteAuthController;
import xyz.spedcord.server.response.Responses;

public class InviteEndpoint extends Endpoint {

    private final InviteAuthController auth;
    private final JoinLinkController joinLinkController;

    public InviteEndpoint(InviteAuthController auth, JoinLinkController joinLinkController) {
        this.auth = auth;
        this.joinLinkController = joinLinkController;
    }

    @Override
    public void handle(Context context) {
        String id = context.pathParam("id");

        int companyId = this.joinLinkController.getCompanyId(id);
        if (companyId == -1) {
            Responses.error("Invalid id param").respondTo(context);
            return;
        }

        context.redirect(this.auth.getNewAuthLink(companyId, id));
    }
}
