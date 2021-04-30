package me.nathan.oauthclient.model.dto.response.api;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {

    private Long id;

    private String name;

    private String image;

    @Builder
    public UserDto(Long id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }
}
