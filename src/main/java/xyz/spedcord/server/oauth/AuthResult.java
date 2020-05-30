package xyz.spedcord.server.oauth;

import bell.oauth.discord.domain.User;
import bell.oauth.discord.main.Response;

public class AuthResult {

    public static final AuthResult ERROR = new AuthResult(Response.ERROR, null, -1, null);

    private final Response response;
    private final User user;
    private final int companyId;
    private final String joinId;

    public AuthResult(Response response, User user, int companyId, String joinId) {
        this.response = response;
        this.user = user;
        this.companyId = companyId;
        this.joinId = joinId;
    }

    public Response getResponse() {
        return response;
    }

    public User getUser() {
        return user;
    }

    public int getCompanyId() {
        return companyId;
    }

    public String getJoinId() {
        return joinId;
    }

}
