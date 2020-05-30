package xyz.spedcord.server.oauth.register;

import bell.oauth.discord.domain.User;
import bell.oauth.discord.main.Response;

public class RegisterAuthResult {
    public static final RegisterAuthResult ERROR = new RegisterAuthResult(Response.ERROR, null);

    private Response response;
    private User user;

    public RegisterAuthResult(Response response, User user) {
        this.response = response;
        this.user = user;
    }

    public static RegisterAuthResult getERROR() {
        return ERROR;
    }

    public Response getResponse() {
        return response;
    }

    public User getUser() {
        return user;
    }

}
