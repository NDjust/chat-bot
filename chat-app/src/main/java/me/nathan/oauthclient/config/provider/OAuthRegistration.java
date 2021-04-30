package me.nathan.oauthclient.config.provider;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class OAuthRegistration {
    private static final String DEFAULT_LOGIN_REDIRECT_URL = "{baseUrl}/login/oauth2/code/{registrationId}";

    public String registrationId;
    public String clientId;
    public String clientSecret;
    public String clientName;
    public String redirectUri;
    public String authorizationGrantType;
    public String scope;
    public String authorizationUri;
    public String tokenUri;
    public String userInfoUri;
    public String userNameAttribute;

    public abstract void setRegistrationId(String registrationId);

    public abstract void setAuthorizationUri(String authorizationUri);

    public abstract void setTokenUri(String tokenUri);

    public abstract void setUserInfoUri(String userInfoUri);

    public abstract void setScope(String scope);

    public abstract void setClientId(String clientId);

    public abstract void setClientSecret(String clientSecret);

    public abstract void setClientName(String clientName);

    public abstract void setUserNameAttribute(String userNameAttrName);

    public abstract void setAuthorizationGrantType(String authorizationGrantType);

    protected final List<String> splitCommand(String s) {
        return Arrays.stream(s.split(","))
                .collect(Collectors.toList());
    }

    public final ClientRegistration.Builder getBuilder() {
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(registrationId);
        builder.clientAuthenticationMethod(ClientAuthenticationMethod.POST);
        builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
        builder.registrationId(registrationId);
        builder.redirectUriTemplate(DEFAULT_LOGIN_REDIRECT_URL);
        builder.scope(splitCommand(scope));
        builder.authorizationUri(authorizationUri);
        builder.tokenUri(tokenUri);
        builder.userInfoUri(userInfoUri);
        builder.clientId(clientId);
        builder.clientSecret(clientSecret);
        builder.userNameAttributeName(userNameAttribute);
        builder.clientName(clientName);
        return builder;
    }
}
