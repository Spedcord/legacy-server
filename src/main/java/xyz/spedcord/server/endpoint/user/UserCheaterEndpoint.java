package xyz.spedcord.server.endpoint.user;

import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.response.Responses;
import xyz.spedcord.server.user.Flag;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class UserCheaterEndpoint extends Endpoint {

    private final UserController userController;

    public UserCheaterEndpoint(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(Context context) {
        Optional<Long> cheaterIdOptional = this.getQueryParamAsLong("cheaterId", context);
        if (cheaterIdOptional.isEmpty()) {
            Responses.error("Invalid cheaterId param").respondTo(context);
            return;
        }

        Optional<User> optional = this.getUserFromQuery("userId", true, context, this.userController);
        if (optional.isEmpty()) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }
        User user = optional.get();

        if (Arrays.stream(SpedcordServer.MODERATORS).noneMatch(l -> l == user.getDiscordId())) {
            Responses.error(HttpStatus.UNAUTHORIZED_401, "Unauthorized").respondTo(context);
            return;
        }

        long cheaterId = cheaterIdOptional.get();
        Optional<User> cheaterOptional = this.userController.getUser(cheaterId);
        if (cheaterOptional.isEmpty()) {
            Responses.error("Unknown user").respondTo(context);
            return;
        }

        User cheater = cheaterOptional.get();
        cheater.setFlags(new ArrayList<>() {
            {
                this.add(Flag.CHEATER);
            }
        });
        this.userController.updateUser(cheater);

        Responses.success("User was flagged as cheater").respondTo(context);
    }
}
