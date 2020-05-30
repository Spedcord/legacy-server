package xyz.spedcord.server.endpoint.company;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.oauth.JoinLinkRetriever;
import xyz.spedcord.server.response.Responses;

public class CreateJoinLinkEndpoint extends RestrictedEndpoint {

    private final JoinLinkRetriever joinLinkRetriever;

    public CreateJoinLinkEndpoint(JoinLinkRetriever joinLinkRetriever) {
        this.joinLinkRetriever = joinLinkRetriever;
    }

    @Override
    protected void handleFurther(Context context) {
        String rawCompanyId = context.queryParam("companyId");
        if (rawCompanyId == null) {
            Responses.error("Missing companyId param").respondTo(context);
            return;
        }
        int companyId;
        try {
            companyId = Integer.parseInt(rawCompanyId);
        } catch (NumberFormatException ignored) {
            Responses.error("Invalid companyId param").respondTo(context);
            return;
        }

        String id = joinLinkRetriever.generateNewLink(companyId);
        context.result(String.format("https://www.spedcord.xyz/invite/%s", id)).status(200);
    }
}
