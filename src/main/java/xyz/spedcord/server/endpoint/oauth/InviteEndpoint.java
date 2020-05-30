package xyz.spedcord.server.endpoint.oauth;

import dev.lukaesebrot.jal.endpoints.Endpoint;
import io.javalin.http.Context;
import xyz.spedcord.server.oauth.DiscordAuthorizationReceiver;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.response.Responses;

import java.sql.SQLException;

public class InviteEndpoint extends Endpoint {

    private DiscordAuthorizationReceiver auth;
    private JoinLinkController joinLinkController;

    public InviteEndpoint(DiscordAuthorizationReceiver auth, JoinLinkController joinLinkController) {
        this.auth = auth;
        this.joinLinkController = joinLinkController;
    }

    @Override
    public void handle(Context context) {
        String id = context.pathParam("id");

        int companyId = joinLinkController.getCompanyId(id);
        if(companyId == -1) {
            Responses.error("Invalid id param").respondTo(context);
            return;
        }

        context.redirect(auth.getNewAuthLink(companyId, id));
    }
}
