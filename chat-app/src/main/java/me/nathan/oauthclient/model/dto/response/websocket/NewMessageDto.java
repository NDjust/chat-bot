package me.nathan.oauthclient.model.dto.response.websocket;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.domain.type.MessageType;
import me.nathan.oauthclient.model.type.ChatMessageType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
public class NewMessageDto {

    @Enumerated(EnumType.STRING)
    private ChatMessageType type;
    private PublishedMessage message;

    @Builder
    public NewMessageDto(ChatMessageType chatMessageType, Long id, MessageType type, Long userId, String content,
                         Long sentAt, Long unreadCnt) {
        this.type = chatMessageType;
        this.message = new PublishedMessage(id, type, userId, content, sentAt, unreadCnt);
    }

    @Data
    @NoArgsConstructor
    public class PublishedMessage {

        private Long id;

        @Enumerated(EnumType.STRING)
        private MessageType type;

        private Long userId;

        private String content;

        private Long sentAt;

        private Long unreadCnt;

        public PublishedMessage(Long id, MessageType type, Long userId, String content, Long sentAt, Long unreadCnt) {
            this.id = id;
            this.type = type;
            this.userId = userId;
            this.content = content;
            this.sentAt = sentAt;
            this.unreadCnt = unreadCnt;
        }
    }


    public MessageUnreadCntDto toMessageUnreadCntDto() {
        return new MessageUnreadCntDto(message.id, Integer.parseInt(String.valueOf(message.unreadCnt)), message.sentAt);
    }
}
