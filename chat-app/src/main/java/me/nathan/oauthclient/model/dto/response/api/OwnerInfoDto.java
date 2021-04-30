package me.nathan.oauthclient.model.dto.response.api;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.domain.type.ProviderType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Data
@NoArgsConstructor
public class OwnerInfoDto {

    private String id;

    private String name;

    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;


    @Builder
    public OwnerInfoDto(String name, String email, String id, String phone, ProviderType providerType) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.providerType = providerType;
    }
}
