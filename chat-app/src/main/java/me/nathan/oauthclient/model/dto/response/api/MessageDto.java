package me.nathan.oauthclient.model.dto.response.api;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.domain.type.MessageType;
import me.nathan.oauthclient.model.dto.response.websocket.MessageUnreadCntDto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Set;

@Data
@NoArgsConstructor
public class MessageDto {

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private Long id;

    private Long userId;

    private String content;

    private Long sentAt;

    private Long unreadCnt;

    private Set<Long> unreadCntUserIds;

    @Builder
    public MessageDto(MessageType type, Long id, Long userId, String content, Long sentAt, Long unreadCnt,
                      Set<Long> unreadCntUserIds) {
        this.type = type;
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.sentAt = sentAt;
        this.unreadCnt = unreadCnt;
        this.unreadCntUserIds = unreadCntUserIds;
    }

    public MessageUnreadCntDto toMessageUnreadCntDto() {
        return new MessageUnreadCntDto(this.id, this.unreadCntUserIds.size(), this.sentAt);
    }
}
