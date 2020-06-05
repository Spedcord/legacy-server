package xyz.spedcord.server.oauth.register;

import bell.oauth.discord.main.OAuthBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegisterAuthController {

    private String clientId;
    private String clientSecret;
    private Map<String, OAuthBuilder> authMap;

    public RegisterAuthController(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authMap = new HashMap<>();
    }

    public String getNewAuthLink() {
        OAuthBuilder oAuthBuilder = new OAuthBuilder(clientId, clientSecret)
                .setScopes(new String[]{"identify", "guilds.join"})
                .setRedirectURI("https://api.spedcord.xyz/user/register/discord");

        String state = UUID.randomUUID().toString();
        authMap.put(state, oAuthBuilder);

        return oAuthBuilder.getAuthorizationUrl(state);
    }

    public RegisterAuthResult exchangeCode(String code, String state) {
        OAuthBuilder oAuthBuilder = authMap.remove(state);
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
