package xyz.spedcord.server.endpoint;

import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.response.Responses;

/**
 * Base class for restricted endpoints
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public abstract class RestrictedEndpoint extends Endpoint {

    /**
     * Check if the context is authorized
     *
     * @param context
     * @return
     */
    public static boolean isAuthorized(Context context) {
        // Get auth header
        String authorization = context.header("Authorization");
        if (authorization == null) {
            return false;
        }

        // Trim string if needed
        if (authorization.startsWith("Bearer ")) {
            authorization = authorization.substring(7);
        }

        // Check for match with key
        return authorization.equals(SpedcordServer.KEY);
    }

    @Override
    public void handle(Context context) {
        // Get auth header
        String authorization = context.header("Authorization");
        if (authorization == null) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        // Trim string if needed
        if (authorization.startsWith("Bearer ")) {
            authorization = authorization.substring(7);
        }

        // Check if matches
        if (!authorization.equals(SpedcordServer.KEY)) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        System.out.println(String.format("[INFO] Restricted endpoint connection from %s", context.ip()));
        this.handleFurther(context);
    }

    protected abstract void handleFurther(Context context);

}
