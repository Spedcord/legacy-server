package xyz.spedcord.server.oauth;

import bell.oauth.discord.main.OAuthBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscordAuthorizationReceiver {

    private String clientId;
    private String clientSecret;
    private Map<String, OAuthBuilder> authMap;
    private Map<String, String> companyIdMap;
    private Map<String, String> joinIdMap;

    public DiscordAuthorizationReceiver(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authMap = new HashMap<>();
        this.companyIdMap = new HashMap<>();
        this.joinIdMap = new HashMap<>();
    }

    public String getNewAuthLink(String companyId, String joinId) {
        OAuthBuilder oAuthBuilder = new OAuthBuilder(clientId, clientSecret)
                .setScopes(new String[]{"identify"})
                .setRedirectURI("http://localhost/discord");

        String state = UUID.randomUUID().toString();
        authMap.put(state, oAuthBuilder);
        companyIdMap.put(state, companyId);
        joinIdMap.put(state, joinId);

        return oAuthBuilder.getAuthorizationUrl(state);
    }

    public AuthResult exchangeCode(String code, String state) {
        OAuthBuilder oAuthBuilder = authMap.remove(state);
        String companyId = companyIdMap.remove(state);
        String joinId = joinIdMap.remove(state);
        if (oAuthBuilder == null || companyId == null || joinId == null) {
            return AuthResult.ERROR;
        }

        return new AuthResult(
                oAuthBuilder.exchange(code),
                oAuthBuilder.getUser(),
                companyId,
                joinId
        );
    }

    private <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new Map.Entry<K, V>() {
            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() {
                return value;
            }

            @Override
            public V setValue(V value) {
                return value;
            }
        };
    }

}
