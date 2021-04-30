package me.nathan.oauthclient.model.dto.response.api;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.domain.type.MessageType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class MessagesDto {

    private List<PreviousMessageDto> messages;

    public MessagesDto(List<MessageDto> messages) {
        this.messages = messages
                .stream()
                .map(messageDto -> PreviousMessageDto
                        .builder()
                        .type(messageDto.getType())
                        .id(messageDto.getId())
                        .userId(messageDto.getUserId())
                        .content(messageDto.getContent())
                        .sentAt(messageDto.getSentAt())
                        .unreadCnt(messageDto.getUnreadCnt())
                        .build())
                .collect(Collectors.toList());
    }

    @Data
    @NoArgsConstructor
    private static class PreviousMessageDto {
        @Enumerated(EnumType.STRING)
        private MessageType type;

        private Long id;

        private Long userId;

        private String content;

        private Long sentAt;

        private Long unreadCnt;

        @Builder
        public PreviousMessageDto(MessageType type, Long id, Long userId, String content, Long sentAt, Long unreadCnt) {
            this.type = type;
            this.id = id;
            this.userId = userId;
            this.content = content;
            this.sentAt = sentAt;
            this.unreadCnt = unreadCnt;
        }
    }
}
