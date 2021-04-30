package me.nathan.oauthclient.model.dto.response.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UsersDto {

    private List<UserDto> users;

    public UsersDto(List<UserDto> userDtos) {
        this.users = userDtos;
    }
}
