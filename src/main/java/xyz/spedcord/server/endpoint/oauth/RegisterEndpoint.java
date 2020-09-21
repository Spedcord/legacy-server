package xyz.spedcord.server.endpoint.oauth;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.oauth.register.RegisterAuthController;

/**
 * Handles a user registration
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class RegisterEndpoint extends Endpoint {

    private final RegisterAuthController authController;

    public RegisterEndpoint(RegisterAuthController authController) {
        this.authController = authController;
    }

    @Override
    public void handle(Context context) {
        // Redirect to auth url
        context.redirect(this.authController.getNewAuthLink());
    }

}
