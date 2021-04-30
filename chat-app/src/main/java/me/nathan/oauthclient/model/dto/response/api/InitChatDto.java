package me.nathan.oauthclient.model.dto.response.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.model.dto.response.websocket.InitCreateChatDto;

@Data
@NoArgsConstructor
public class InitChatDto {

    private InitCreateChatDto chat;

    public InitChatDto(InitCreateChatDto chatDto) {
        this.chat = chatDto;
    }
}
