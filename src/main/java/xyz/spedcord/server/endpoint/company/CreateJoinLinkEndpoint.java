package xyz.spedcord.server.endpoint.company;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.response.Responses;

public class CreateJoinLinkEndpoint extends RestrictedEndpoint {

    private final JoinLinkController joinLinkController;

    public CreateJoinLinkEndpoint(JoinLinkController joinLinkController) {
        this.joinLinkController = joinLinkController;
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

        int maxUses = 1;
        String rawMaxUses = context.queryParam("maxUses");
        if (rawMaxUses != null) {
            try {
                maxUses = Integer.parseInt(rawMaxUses);
            } catch (NumberFormatException ignored) {
                Responses.error("Invalid maxUses param").respondTo(context);
                return;
            }
        }

        String id = joinLinkController.generateNewLink(companyId, maxUses);
        context.result(String.format("https://www.spedcord.xyz/invite/%s", id)).status(200);
    }
}
