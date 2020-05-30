package xyz.spedcord.server.endpoint.oauth;

import dev.lukaesebrot.jal.endpoints.Endpoint;
import io.javalin.http.Context;
import xyz.spedcord.server.oauth.register.RegisterAuthController;

public class RegisterEndpoint extends Endpoint {

    private final RegisterAuthController authController;

    public RegisterEndpoint(RegisterAuthController authController) {
        this.authController = authController;
    }

    @Override
    public void handle(Context context) {
        context.redirect(authController.getNewAuthLink());
    }
}
