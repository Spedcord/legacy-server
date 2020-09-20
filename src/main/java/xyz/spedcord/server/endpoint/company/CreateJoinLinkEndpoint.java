package xyz.spedcord.server.endpoint.company;

import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.RestrictedEndpoint;
import xyz.spedcord.server.joinlink.JoinLinkController;
import xyz.spedcord.server.response.Responses;

import java.util.Optional;

public class CreateJoinLinkEndpoint extends RestrictedEndpoint {

    private final JoinLinkController joinLinkController;
    private final int port;
    private String host;

    public CreateJoinLinkEndpoint(JoinLinkController joinLinkController, String host, int port) {
        this.joinLinkController = joinLinkController;
        this.host = host;
        this.port = port;

        if (this.host.equals("spedcord.xyz")) {
            this.host = "www." + this.host;
        }
    }

    @Override
    protected void handleFurther(Context context) {
        Optional<Integer> paramOptional = this.getPathParamAsInt("companyId", context);
        if (paramOptional.isEmpty()) {
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

        String customId = this.getQueryParam("customId", context).orElse(null);
        String id = (customId == null ? this.joinLinkController.generateNewLink(companyId, maxUses)
                : this.joinLinkController.addCustomLink(customId, companyId, maxUses));
        context.result(String.format((SpedcordServer.DEV ? "http://localhost:81" : "https://api.spedcord.xyz") + "/invite/%s", id)).status(200);
    }
}
