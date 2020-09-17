package xyz.spedcord.server.endpoint;

import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.response.Responses;

public abstract class RestrictedEndpoint extends Endpoint {
    @Override
    public void handle(Context context) {
        String authorization = context.header("Authorization");
        if (authorization == null) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        if (authorization.startsWith("Bearer ")) {
            authorization = authorization.substring(7);
        }

        if (!authorization.equals(SpedcordServer.KEY)) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").addData("yk", authorization).addData("ok", SpedcordServer.KEY).respondTo(context);
            return;
        }

        System.out.println(String.format("[INFO] Restricted endpoint connection from %s", context.ip()));
        handleFurther(context);
    }

    protected abstract void handleFurther(Context context);
}
