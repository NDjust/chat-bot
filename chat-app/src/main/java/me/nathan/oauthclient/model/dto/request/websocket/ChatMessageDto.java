package me.nathan.oauthclient.model.dto.request.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.domain.type.MessageType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
public class ChatMessageDto {

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private Long chatId;

    private String content;

    private Long timeStamp;

    public ChatMessageDto(MessageType type, Long chatId, String content) {
        this.type = type;
        this.chatId = chatId;
        this.content = content;
    }


}
