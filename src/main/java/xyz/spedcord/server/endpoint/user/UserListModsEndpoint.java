package xyz.spedcord.server.endpoint.user;

import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.Endpoint;

/**
 * Lists the moderators
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class UserListModsEndpoint extends Endpoint {

    @Override
    public void handle(Context ctx) {
        ctx.status(200).result(SpedcordServer.GSON.toJson(SpedcordServer.MODERATORS));
    }

}
