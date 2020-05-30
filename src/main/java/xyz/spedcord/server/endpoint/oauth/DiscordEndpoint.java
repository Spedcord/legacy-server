package xyz.spedcord.server.endpoint.oauth;

import com.google.gson.Gson;
import dev.lukaesebrot.jal.endpoints.Endpoint;
import io.javalin.http.Context;
import xyz.spedcord.server.oauth.AuthResult;
import xyz.spedcord.server.oauth.DiscordAuthorizationReceiver;
import xyz.spedcord.server.oauth.JoinLinkRetriever;

public class DiscordEndpoint extends Endpoint {

    private DiscordAuthorizationReceiver auth;
    private JoinLinkRetriever joinLinkRetriever;

    public DiscordEndpoint(DiscordAuthorizationReceiver auth, JoinLinkRetriever joinLinkRetriever) {
        this.auth = auth;
        this.joinLinkRetriever = joinLinkRetriever;
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
        joinLinkRetriever.removeJoinLink(authResult.getJoinId());

        //TODO: Actually add the user to the company
        context.result(new Gson().toJson(authResult)).status(200);
    }
}
