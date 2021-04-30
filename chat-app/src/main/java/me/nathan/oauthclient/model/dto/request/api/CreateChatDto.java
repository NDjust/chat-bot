package me.nathan.oauthclient.model.dto.request.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateChatDto {

    private String image;

    private String name;

    private List<Long> friendIds;

    public CreateChatDto(String image, String name, List<Long> friends) {
        this.image = image;
        this.name = name;
        this.friendIds = friends;
    }
}
