package me.nathan.oauthclient.model.dto.response.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WaitingParticipantChatDto {

    private Long userId;

    private Long chatId;

    public WaitingParticipantChatDto(Long userId, Long chatId) {
        this.userId = userId;
        this.chatId = chatId;
    }
}
