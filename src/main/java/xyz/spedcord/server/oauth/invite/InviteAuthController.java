package xyz.spedcord.server.oauth.invite;

import bell.oauth.discord.main.OAuthBuilder;
import xyz.spedcord.server.SpedcordServer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteAuthController {

    private final String clientId;
    private final String clientSecret;
    private final Map<String, OAuthBuilder> authMap;
    private final Map<String, Integer> companyIdMap;
    private final Map<String, String> joinIdMap;

    public InviteAuthController(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authMap = new HashMap<>();
        this.companyIdMap = new HashMap<>();
        this.joinIdMap = new HashMap<>();
    }

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

    private <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new Map.Entry<>() {
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
