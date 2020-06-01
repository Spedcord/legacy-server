package xyz.spedcord.server.oauth.register;

import bell.oauth.discord.domain.User;
import bell.oauth.discord.main.Response;

public class RegisterAuthResult {
    public static final RegisterAuthResult ERROR = new RegisterAuthResult(Response.ERROR, null, null, null, -1);

    private Response response;
    private User user;
    private String accessToken;
    private String refreshToken;
    private long tokenExpires;

    public RegisterAuthResult(Response response, User user, String accessToken, String refreshToken, long tokenExpires) {
        this.response = response;
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpires = tokenExpires;
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

    public long getTokenExpires() {
        return tokenExpires;
    }
}
