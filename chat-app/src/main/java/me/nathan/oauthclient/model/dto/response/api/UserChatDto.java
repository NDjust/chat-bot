package me.nathan.oauthclient.model.dto.response.api;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.domain.type.ChatType;
import me.nathan.oauthclient.model.dao.MemberInfo;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Data
@NoArgsConstructor
public class UserChatDto {

    private Long id;

    private String image;

    private String name;

    private String lastMessage;

    private Long lastAt;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    private Long unreadCnt;

    private List<MemberInfo> members;

    @Builder
    public UserChatDto(Long id, String name, String image, String lastMessage,
                          ChatType type, Long lastAt, Long unreadCnt, List<MemberInfo> members) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.lastMessage = lastMessage;
        this.type = type;
        this.lastAt = lastAt;
        this.unreadCnt = unreadCnt;
        this.members = members;
    }
}
