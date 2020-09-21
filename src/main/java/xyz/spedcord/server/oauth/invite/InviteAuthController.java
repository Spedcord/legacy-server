package xyz.spedcord.server.oauth.invite;

import bell.oauth.discord.main.OAuthBuilder;
import xyz.spedcord.server.SpedcordServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A controller that handles everything related to company invites
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class InviteAuthController {

    private final String clientId;
    private final String clientSecret;
    private final Map<String, OAuthBuilder> authMap;
    private final Map<String, Integer> companyIdMap;
    private final Map<String, String> joinIdMap;

    /**
     * Constructs a new instance of this controller
     *
     * @param clientId     Client id of the Discord application
     * @param clientSecret Client secret of the Discord application
     */
    public InviteAuthController(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authMap = new HashMap<>();
        this.companyIdMap = new HashMap<>();
        this.joinIdMap = new HashMap<>();
    }

    /**
     * Creates a new authorization url
     *
     * @param companyId Id of the company
     * @param joinId    Id of the join link
     * @return A newly generated authorization url
     */
    public String getNewAuthLink(int companyId, String joinId) {
        OAuthBuilder oAuthBuilder = new OAuthBuilder(this.clientId, this.clientSecret)
                .setScopes(new String[]{"identify"})
                .setRedirectURI((SpedcordServer.DEV ? "http://localhost:81" : "https://api.spedcord.xyz") + "/invite/discord");

        String state = UUID.randomUUID().toString();
        this.authMap.put(state, oAuthBuilder);
        this.companyIdMap.put(state, companyId);
        this.joinIdMap.put(state, joinId);

        return oAuthBuilder.getAuthorizationUrl(state);
    }

    /**
     * Exchanges the authorization code
     *
     * @param code  The authorization code
     * @param state The unique state
     * @return The result
     */
    public InviteAuthResult exchangeCode(String code, String state) {
        OAuthBuilder oAuthBuilder = this.authMap.remove(state);
        Integer companyId = this.companyIdMap.remove(state);
        String joinId = this.joinIdMap.remove(state);
        if (oAuthBuilder == null || companyId == null || joinId == null) {
            return InviteAuthResult.ERROR;
        }

        return new InviteAuthResult(
                oAuthBuilder.exchange(code),
                oAuthBuilder.getUser(),
                companyId,
                joinId
        );
    }

}
