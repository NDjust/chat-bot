package me.nathan.oauthclient.model.dto.response.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckDto {

    UserDto user;

    public CheckDto(UserDto user) {
        this.user = user;
    }
}
