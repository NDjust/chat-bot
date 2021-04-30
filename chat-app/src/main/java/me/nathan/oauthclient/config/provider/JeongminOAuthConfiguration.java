package me.nathan.oauthclient.config.provider;

import org.springframework.beans.factory.annotation.Value;

public class JeongminOAuthConfiguration extends OAuthRegistration {

    @Override
    @Value("${spring.security.oauth2.client.registration.jeongmin.registration-id}")
    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    @Value("${spring.security.oauth2.client.registration.jeongmin.client-id}")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Value("${spring.security.oauth2.client.registration.jeongmin.client-secret}")
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Value("${spring.security.oauth2.client.registration.jeongmin.client-name}")
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Value("${spring.security.oauth2.client.registration.jeongmin.redirect-uri}")
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Value("${spring.security.oauth2.client.registration.jeongmin.authorization-grant-type}")
    public void setAuthorizationGrantType(String authorizationGrantType) {
        this.authorizationGrantType = authorizationGrantType;
    }

    @Value("${spring.security.oauth2.client.registration.jeongmin.scope}")
    public void setScope(String scope) {
        this.scope = scope;
    }

    @Value("${spring.security.oauth2.client.provider.jeongmin.authorization-uri}")
    public void setAuthorizationUri(String authorizationUri) {
        this.authorizationUri = authorizationUri;
    }

    @Value("${spring.security.oauth2.client.provider.jeongmin.token-uri}")
    public void setTokenUri(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    @Value("${spring.security.oauth2.client.provider.jeongmin.user-info-uri}")
    public void setUserInfoUri(String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }

    @Value("${spring.security.oauth2.client.provider.jeongmin.user-name-attribute}")
    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }


}
