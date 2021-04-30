package me.nathan.oauthclient.domain.type;

import java.util.Arrays;

public enum ProviderType {
    NATHAN("nathan"),
    WOOJAE("woojae"),
    JEONGMIN("jeongmin"),
    DAEUN("daeun");

    private String name;

    ProviderType(String name) {
        this.name = name;
    }

    public static ProviderType findByName(String name) {
        return Arrays.stream(values())
                .filter(providerType -> providerType.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(name + " - 해당하는 OAuthTpye이 존재하지 않습니다."));
    }
}
