package me.nathan.oauthclient.model.dto.request.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JoinChatDto {

    private Long chatId;

    private List<Long> invitedIds;

    public JoinChatDto(Long chatId, List<Long> invitedIds) {
        this.chatId = chatId;
        this.invitedIds = invitedIds;
    }
}
