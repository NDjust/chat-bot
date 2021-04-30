package me.nathan.oauthclient.model.dto.response.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.model.type.ChatMessageType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
public class ChatOutUserDto {

    @Enumerated(EnumType.STRING)
    private ChatMessageType type;

    private Long userId;


    public ChatOutUserDto(ChatMessageType type, Long userId) {
        this.type = type;
        this.userId = userId;
    }
}
