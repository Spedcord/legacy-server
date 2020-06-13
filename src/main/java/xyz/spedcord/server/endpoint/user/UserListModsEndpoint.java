package xyz.spedcord.server.endpoint.user;

import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.Endpoint;

public class UserListModsEndpoint extends Endpoint {
    @Override
    public void handle(Context ctx) {
        ctx.status(200).result(SpedcordServer.GSON.toJson(SpedcordServer.MODERATORS));
    }
}
