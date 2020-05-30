package xyz.spedcord.server.endpoint.oauth;

import dev.lukaesebrot.jal.endpoints.Endpoint;
import io.javalin.http.Context;
import xyz.spedcord.server.oauth.DiscordAuthorizationReceiver;
import xyz.spedcord.server.oauth.JoinLinkRetriever;
import xyz.spedcord.server.response.Responses;

import java.sql.SQLException;

public class InviteEndpoint extends Endpoint {

    private DiscordAuthorizationReceiver auth;
    private JoinLinkRetriever joinLinkRetriever;

    public InviteEndpoint(DiscordAuthorizationReceiver auth, JoinLinkRetriever joinLinkRetriever) {
        this.auth = auth;
        this.joinLinkRetriever = joinLinkRetriever;
    }

    @Override
    public void handle(Context context) {
        String id = context.pathParam("id");

        int companyId = -1;
        try {
            companyId = joinLinkRetriever.getCompanyId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(companyId == -1) {
            Responses.error("Invalid id param").respondTo(context);
            return;
        }

        context.redirect(auth.getNewAuthLink(companyId, id));
    }
}
