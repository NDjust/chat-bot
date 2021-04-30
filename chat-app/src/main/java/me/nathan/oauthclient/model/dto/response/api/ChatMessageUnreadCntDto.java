package me.nathan.oauthclient.model.dto.response.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMessageUnreadCntDto {

    private Long chatId;

    private Long unreadCnt;


    public ChatMessageUnreadCntDto(Long chatId, Long unreadCnt) {
        this.chatId = chatId;
        this.unreadCnt = unreadCnt;
    }

    public ChatMessageUnreadCntDto(Long chatId, Integer unreadCnt) {
        this.chatId = chatId;
        this.unreadCnt = (long) unreadCnt;
    }
}
