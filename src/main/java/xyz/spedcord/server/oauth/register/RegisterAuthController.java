package xyz.spedcord.server.oauth.register;

import bell.oauth.discord.main.OAuthBuilder;
import xyz.spedcord.server.SpedcordServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A controller that handles everything to user registration
 *
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class RegisterAuthController {

    private final String clientId;
    private final String clientSecret;
    private final Map<String, OAuthBuilder> authMap;

    /**
     * Constructs a new instance of this controller
     *
     * @param clientId     Client id of the Discord application
     * @param clientSecret Client secret of the Discord application
     */
    public RegisterAuthController(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authMap = new HashMap<>();
    }

    /**
     * Creates a new authorization url
     *
     * @return The newly generated authorization url
     */
    public String getNewAuthLink() {
        OAuthBuilder oAuthBuilder = new OAuthBuilder(this.clientId, this.clientSecret)
                .setScopes(new String[]{"identify", "guilds.join"})
                .setRedirectURI((SpedcordServer.DEV ? "http://localhost:81" : "https://api.spedcord.xyz") + "/user/register/discord");

        String state = UUID.randomUUID().toString();
        this.authMap.put(state, oAuthBuilder);

        return oAuthBuilder.getAuthorizationUrl(state);
    }

    /**
     * Exchanges the authorization code
     *
     * @param code  The authorization code
     * @param state The unique state
     * @return The result
     */
    public RegisterAuthResult exchangeCode(String code, String state) {
        OAuthBuilder oAuthBuilder = this.authMap.remove(state);
        if (oAuthBuilder == null) {
            return RegisterAuthResult.ERROR;
        }

        return new RegisterAuthResult(
                oAuthBuilder.exchange(code),
                oAuthBuilder.getUser(),
                oAuthBuilder.getAccess_token(),
                oAuthBuilder.getRefresh_token(),
                oAuthBuilder.getTokenExpiresIn()
        );
    }

}
