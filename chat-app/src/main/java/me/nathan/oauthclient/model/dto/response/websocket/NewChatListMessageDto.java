package me.nathan.oauthclient.model.dto.response.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.model.type.ChatListType;
import me.nathan.oauthclient.model.dto.response.api.ChatDto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
public class NewChatListMessageDto {

    @Enumerated(EnumType.STRING)
    private ChatListType type;

    private ChatDto chat;

    public NewChatListMessageDto(ChatListType type, ChatDto chat) {
        this.type = type;
        this.chat = chat;
    }
}
