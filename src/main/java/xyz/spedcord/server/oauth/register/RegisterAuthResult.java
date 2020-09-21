package xyz.spedcord.server.oauth.register;

import bell.oauth.discord.domain.User;
import bell.oauth.discord.main.Response;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class RegisterAuthResult {
    public static final RegisterAuthResult ERROR = new RegisterAuthResult(Response.ERROR, null, null, null, -1);

    private final Response response;
    private final User user;
    private final String accessToken;
    private final String refreshToken;
    private final long tokenExpires;

    public RegisterAuthResult(Response response, User user, String accessToken, String refreshToken, long tokenExpires) {
        this.response = response;
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpires = tokenExpires;
    }

    public Response getResponse() {
        return this.response;
    }

    public User getUser() {
        return this.user;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public long getTokenExpires() {
        return this.tokenExpires;
    }
}
