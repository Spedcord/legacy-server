package xyz.spedcord.server.endpoint.company;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.response.Responses;

import java.util.Optional;

public class CreateJoinLinkEndpoint extends RestrictedEndpoint {

    private final JoinLinkController joinLinkController;

    public CreateJoinLinkEndpoint(JoinLinkController joinLinkController) {
        this.joinLinkController = joinLinkController;
    }

    @Override
    protected void handleFurther(Context context) {
        Optional<Integer> paramOptional = getPathParamAsInt("companyId", context);
        if(paramOptional.isEmpty()) {
            Responses.error("Invalid companyId param").respondTo(context);
            return;
        }
        int companyId = paramOptional.get();

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
