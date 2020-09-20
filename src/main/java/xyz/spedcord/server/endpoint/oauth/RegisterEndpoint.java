package xyz.spedcord.server.endpoint.oauth;

import io.javalin.http.Context;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.oauth.register.RegisterAuthController;

public class RegisterEndpoint extends Endpoint {

    private final RegisterAuthController authController;

    public RegisterEndpoint(RegisterAuthController authController) {
        this.authController = authController;
    }

    @Override
    public void handle(Context context) {
        context.redirect(this.authController.getNewAuthLink());
    }
}
