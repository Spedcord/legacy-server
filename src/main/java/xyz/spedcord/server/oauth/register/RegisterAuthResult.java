package xyz.spedcord.server.oauth.register;

import bell.oauth.discord.domain.User;
import bell.oauth.discord.main.Response;

public class RegisterAuthResult {
    public static final RegisterAuthResult ERROR = new RegisterAuthResult(Response.ERROR, null, null, null);

    private Response response;
    private User user;
    private String accessToken;
    private String refreshToken;

    public RegisterAuthResult(Response response, User user, String accessToken, String refreshToken) {
        this.response = response;
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Response getResponse() {
        return response;
    }

    public User getUser() {
        return user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

}
