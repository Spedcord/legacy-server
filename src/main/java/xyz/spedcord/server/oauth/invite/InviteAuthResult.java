package xyz.spedcord.server.oauth.invite;

import bell.oauth.discord.domain.User;
import bell.oauth.discord.main.Response;

public class InviteAuthResult {

    public static final InviteAuthResult ERROR = new InviteAuthResult(Response.ERROR, null, -1, null);

    private final Response response;
    private final User user;
    private final int companyId;
    private final String joinId;

    public InviteAuthResult(Response response, User user, int companyId, String joinId) {
        this.response = response;
        this.user = user;
        this.companyId = companyId;
        this.joinId = joinId;
    }

    public Response getResponse() {
        return this.response;
    }

    public User getUser() {
        return this.user;
    }

    public int getCompanyId() {
        return this.companyId;
    }

    public String getJoinId() {
        return this.joinId;
    }

}
