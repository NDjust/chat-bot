package me.nathan.oauthclient.service;

import lombok.Builder;
import lombok.Getter;
import me.nathan.oauthclient.domain.User;
import me.nathan.oauthclient.domain.type.ProviderType;
import me.nathan.oauthclient.domain.type.Role;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String id;
    private String name;
    private String email;
    private String phone;
    private String registrationId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes,
                           String nameAttributeKey, String id,
                           String name, String phone, String email,
                           String registrationId) {
        this.attributes = attributes;
        this.nameAttributeKey= nameAttributeKey;
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.registrationId = registrationId;
    }

    public static OAuthAttributes of(String registrationId,
                                     String userNameAttributeName,
                                     Map<String, Object> attributes) {
        System.out.println("Attributes");
        System.out.println(attributes);

        if (registrationId.equals("nathan")) {
            return ofNadan(registrationId, userNameAttributeName, attributes);
        }

        if (registrationId.equals("woojae")) {
            return ofWoojae(registrationId, userNameAttributeName, attributes);
        }

        if (registrationId.equals("jeongmin")) {
            return ofJeongmin(registrationId, userNameAttributeName, attributes);
        }
        return OAuthAttributes.builder()
                .id((String) attributes.get("id"))
                .name((String) attributes.get("name"))
                .phone((String) attributes.get("phone"))
                .attributes(attributes)
                .registrationId(registrationId)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofJeongmin(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .id((String) attributes.get("id"))
                .name((String) attributes.get("name"))
                .phone((String) attributes.get("phone"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .registrationId(registrationId)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofWoojae(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .id((String) attributes.get("id"))
                .name((String) attributes.get("name"))
                .phone((String) attributes.get("phone"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .registrationId(registrationId)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }


    public static OAuthAttributes ofNadan(String registrationId, String userNameAttributeName,
                                          Map<String, Object> attributes) {

        return OAuthAttributes.builder()
                .id((String) attributes.get("id"))
                .name((String) attributes.get("name"))
                .phone((String) attributes.get("phone"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .registrationId(registrationId)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public User toEntity() {
        return User.builder()
                .username(id)
                .name(name)
                .phone(phone)
                .email(email)
                .providerType(ProviderType.findByName(registrationId))
                .role(Role.USER)
                .build();
    }
}
