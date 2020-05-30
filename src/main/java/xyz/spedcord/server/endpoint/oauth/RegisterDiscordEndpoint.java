package xyz.spedcord.server.endpoint.oauth;

import bell.oauth.discord.main.Response;
import xyz.spedcord.server.endpoint.Endpoint;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.oauth.register.RegisterAuthController;
import xyz.spedcord.server.oauth.register.RegisterAuthResult;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.Optional;

public class RegisterDiscordEndpoint extends Endpoint {

    private final RegisterAuthController authController;
    private final UserController userController;

    public RegisterDiscordEndpoint(RegisterAuthController authController, UserController userController) {
        this.authController = authController;
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        String code = context.queryParam("code");
        if (code == null) {
            //context.status(400);
            context.redirect("https://www.spedcord.xyz/error/user/1");
            return;
        }

        String state = context.queryParam("state");
        if (state == null) {
            //context.status(400);
            context.redirect("https://www.spedcord.xyz/error/user/1");
            return;
        }

        RegisterAuthResult authResult = authController.exchangeCode(code, state);
        if(authResult.getResponse() == Response.ERROR) {
            //Responses.error("Failed").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/user/1");
            return;
        }
        if (authResult.getUser().isBot()) {
            //Responses.error(HttpStatus.FORBIDDEN_403, "You're a bot o.0").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/user/2");
            return;
        }

        Optional<User> optional = userController.getUser(Long.parseLong(authResult.getUser().getId()));
        if(optional.isPresent()) {
            //Responses.error("This Discord account is already registered").respondTo(context);
            context.redirect("https://www.spedcord.xyz/error/user/3");
            return;
        }

        userController.createUser(Long.parseLong(authResult.getUser().getId()));
        //Responses.success("Your Discord account was successfully registered").respondTo(context);
        context.redirect("https://www.spedcord.xyz/success/user/1");
    }
}
