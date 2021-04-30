package me.nathan.oauthclient.model.dto.request.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewFriendDto {

    private Long id;


    public NewFriendDto(Long id) {
        this.id = id;
    }
}
