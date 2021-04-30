package me.nathan.oauthclient.model.dao;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberInfo {

    private Long id;

    private String name;

    private String image;

    private boolean active;

    @Builder
    public MemberInfo(Long id, String name, String image, boolean active) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.active = active;
    }
}
