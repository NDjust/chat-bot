package me.nathan.oauthclient.model.dto.response.websocket;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.model.dto.response.api.UserDto;
import me.nathan.oauthclient.domain.type.ChatType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Data
@NoArgsConstructor
public class InitCreateChatDto {

    private Long id;

    private String image;

    private String name;

    private String lastMessage;

    private Long lastAt;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    private Long unreadCnt;

    private List<UserDto> members;

    @Builder
    public InitCreateChatDto(Long id, String image, String name, String lastMessage,
                   Long lastAt, ChatType type, Long unreadCnt, List<UserDto> members) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastAt = lastAt;
        this.type = type;
        this.unreadCnt = unreadCnt;
        this.members = members;
    }
}
