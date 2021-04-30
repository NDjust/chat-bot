package me.nathan.oauthclient.model.dto.response.websocket;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.model.type.ChatListType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
public class ChatListMessageDto {

    @Enumerated(EnumType.STRING)
    private ChatListType type;

    private LastMessageDto chat;


    @Builder
    public ChatListMessageDto(ChatListType type, Long id, String lastMessage, Long lastAt, Long unreadCnt) {
        this.type = type;
        this.chat = new LastMessageDto(id, lastMessage, lastAt, unreadCnt);
    }

    @Data
    @NoArgsConstructor
    private class LastMessageDto {

        private Long id;

        private String lastMessage;

        private Long lastAt;

        private Long unreadCnt;

        public LastMessageDto(Long id, String lastMessage, Long lastAt, Long unreadCnt) {
            this.id = id;
            this.lastMessage = lastMessage;
            this.lastAt = lastAt;
            this.unreadCnt = unreadCnt;
        }
    }
}
