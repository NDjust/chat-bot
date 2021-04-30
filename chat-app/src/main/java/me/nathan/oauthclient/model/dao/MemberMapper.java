package me.nathan.oauthclient.model.dao;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberMapper {
    private Long id;

    private String name;

    private String image;

    private boolean active;

    private Long lastOutAt;

    private Long lastEnterAt;

    @Builder
    public MemberMapper(Long id, String name, String image, boolean active, Long lastOutAt, Long lastEnterAt) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.active = active;
        this.lastOutAt = lastOutAt;
        this.lastEnterAt = lastEnterAt;
    }
}
