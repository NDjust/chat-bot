package me.nathan.oauthclient.config.provider;

import me.nathan.oauthclient.config.provider.NathanOAuthConfiguration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CustomOAuthProvider {

    NATHAN {
        @Override
        public ClientRegistration.Builder getBuilder() {
            NathanOAuthConfiguration nathanOAuthConfiguration = new NathanOAuthConfiguration();
            return getBuilder(nathanOAuthConfiguration.registrationId)
                    .clientId(nathanOAuthConfiguration.clientId)
                    .clientSecret(nathanOAuthConfiguration.clientSecret)
                    .clientName(nathanOAuthConfiguration.clientName)
                    .scope(splitCommand(nathanOAuthConfiguration.scope))
                    .authorizationUri(nathanOAuthConfiguration.authorizationUri)
                    .tokenUri(nathanOAuthConfiguration.tokenUri)
                    .userInfoUri(nathanOAuthConfiguration.userInfoUri)
                    .userNameAttributeName(nathanOAuthConfiguration.userNameAttribute);
        }
    },
    WOOJAE {
        @Override
        public ClientRegistration.Builder getBuilder() {
            WoojaeOAuthConfiguration woojaeOAuthConfiguration = new WoojaeOAuthConfiguration();
            return getBuilder(woojaeOAuthConfiguration.registrationId)
                    .clientId(woojaeOAuthConfiguration.clientId)
                    .clientSecret(woojaeOAuthConfiguration.clientSecret)
                    .clientName(woojaeOAuthConfiguration.clientName)
                    .scope(splitCommand(woojaeOAuthConfiguration.scope))
                    .authorizationUri(woojaeOAuthConfiguration.authorizationUri)
                    .tokenUri(woojaeOAuthConfiguration.tokenUri)
                    .userInfoUri(woojaeOAuthConfiguration.userInfoUri)
                    .userNameAttributeName(woojaeOAuthConfiguration.userNameAttribute);
        }
    },
    JEONGMIN {
        @Override
        public ClientRegistration.Builder getBuilder() {
            JeongminOAuthConfiguration jeongminOAuthConfiguration = new JeongminOAuthConfiguration();
            return getBuilder(jeongminOAuthConfiguration.registrationId)
                    .clientId(jeongminOAuthConfiguration.clientId)
                    .clientSecret(jeongminOAuthConfiguration.clientSecret)
                    .clientName(jeongminOAuthConfiguration.clientName)
                    .scope(splitCommand(jeongminOAuthConfiguration.scope))
                    .authorizationUri(jeongminOAuthConfiguration.authorizationUri)
                    .tokenUri(jeongminOAuthConfiguration.tokenUri)
                    .userInfoUri(jeongminOAuthConfiguration.userInfoUri)
                    .userNameAttributeName(jeongminOAuthConfiguration.userNameAttribute);
        }
    },
    DAEUN {
        @Override
        public ClientRegistration.Builder getBuilder() {
            DaeunOAuthConfiguration configuration = new DaeunOAuthConfiguration();
            return getBuilder(configuration.registrationId)
                    .clientId(configuration.clientId)
                    .clientSecret(configuration.clientSecret)
                    .clientName(configuration.clientName)
                    .scope(splitCommand(configuration.scope))
                    .authorizationUri(configuration.authorizationUri)
                    .tokenUri(configuration.tokenUri)
                    .userInfoUri(configuration.userInfoUri)
                    .userNameAttributeName(configuration.userNameAttribute);
        }
    };

    private static final String DEFAULT_LOGIN_REDIRECT_URL ="{baseUrl}/login/oauth2/code/{registrationId}";

    // default Set
    protected final ClientRegistration.Builder getBuilder(
            String registrationId) {
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(registrationId);
        builder.redirectUriTemplate(DEFAULT_LOGIN_REDIRECT_URL);
        builder.clientAuthenticationMethod(ClientAuthenticationMethod.POST);
        builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
        return builder;
    }

    public abstract ClientRegistration.Builder getBuilder();

    protected final List<String> splitCommand(String s) {
        return Arrays.stream(s.split(","))
                .collect(Collectors.toList());
    }
}
