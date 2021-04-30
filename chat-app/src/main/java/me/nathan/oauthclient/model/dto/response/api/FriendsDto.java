package me.nathan.oauthclient.model.dto.response.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FriendsDto {
    private List<UserDto> friends;

    public FriendsDto(List<UserDto> friends) {
        this.friends = friends;
    }
}
