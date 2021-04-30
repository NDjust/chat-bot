package me.nathan.oauthclient.service;

import me.nathan.oauthclient.model.dto.response.api.OwnerInfoDto;
import me.nathan.oauthclient.domain.type.ProviderType;
import me.nathan.oauthclient.domain.User;
import me.nathan.oauthclient.domain.UserPrincipal;
import me.nathan.oauthclient.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class CustomOAuthUserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Autowired
    public CustomOAuthUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * OAuth 로그인 시, 해당 유저 디비에 저장 & 요청한 유저 데이터 갱신.
     *
     * @param userRequest : OAuth User Request
     * @return OAuth 로그인이 이루어진 UserPrincipal 객체.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.
                of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        User user = saveOrUpdate(attributes);

        return UserPrincipal.create(user, attributes.getAttributes());

    };

    private User saveOrUpdate(OAuthAttributes attributes) {
        String provideType = ProviderType.findByName(attributes.getRegistrationId()).name();
        User user = userRepository.findByEmailAndProviderType(attributes.getEmail(), provideType)
                .map(entity -> entity.update(
                        attributes.getId(), attributes.getEmail(), attributes.getName(), attributes.getPhone()))
                .orElse(attributes.toEntity());

        return userRepository.save(user);
    }

    public OwnerInfoDto getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                                    .orElseThrow(() -> new NoSuchElementException("등록된 회원이 없습니다."));

        return OwnerInfoDto
                .builder()
                .id(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .providerType(user.getProviderType())
                .build();
    }
}
