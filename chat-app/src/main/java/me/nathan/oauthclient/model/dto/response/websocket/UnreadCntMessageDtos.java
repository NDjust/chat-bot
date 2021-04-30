package me.nathan.oauthclient.model.dto.response.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.model.dto.response.api.ChatMessageUnreadCntDto;
import me.nathan.oauthclient.model.type.ChatMessageType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UnreadCntMessageDtos implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    @Enumerated(EnumType.STRING)
    private ChatMessageType type;

    private Set<MessageUnreadDto> messages;

    public UnreadCntMessageDtos(ChatMessageType type, Set<MessageUnreadDto> messages) {
        this.type = type;
        this.messages = messages;
    }

    public static UnreadCntMessageDtos of(ChatMessageType type, List<MessageUnreadCntDto> messages) {
        Set<MessageUnreadDto> set = new LinkedHashSet<>();
        messages
                .forEach(message -> set.add(new MessageUnreadDto(message.getId(), message.getUnreadCnt())));

        return new UnreadCntMessageDtos(type, set);
    }

    public void addAll(Set<MessageUnreadDto> messages) {
        messages.addAll(messages);
    }
}
