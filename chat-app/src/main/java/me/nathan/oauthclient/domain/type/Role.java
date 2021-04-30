package me.nathan.oauthclient.domain.type;

public enum  Role {

    USER("ROLE_USER", "일반 사용자");

    private final String key;
    private final String value;

    Role(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
