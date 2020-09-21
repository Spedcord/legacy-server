package xyz.spedcord.server.endpoint.user;

import io.javalin.http.Context;
import xyz.spedcord.server.SpedcordServer;
import xyz.spedcord.server.endpoint.Endpoint;
import xyz.spedcord.server.user.User;
import xyz.spedcord.server.user.UserController;

/**
 * Lists the moderators
 *
 * @author Maximilian Dorn
 * @version 2.1.2
 * @since 1.0.0
 */
public class UserListModsEndpoint extends Endpoint {

    private final UserController userController;

    public UserListModsEndpoint(UserController userController) {
        this.userController = userController;
    }

    @Override
    public void handle(Context ctx) {
        ctx.status(200).result(SpedcordServer.GSON.toJson(this.userController.getUsers().stream()
                .filter(user -> user.getAccountType() != User.AccountType.USER)
                .toArray(User[]::new)));
    }

}
