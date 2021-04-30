package me.nathan.oauthclient.model.dto.response.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.model.dao.UserChatMapper;

import java.util.List;

@Data
@NoArgsConstructor
public class ChatsDto {

    private List<UserChatDto> chats;

    public ChatsDto(List<UserChatDto> userChatMappers) {
        this.chats = userChatMappers;
    }

}
