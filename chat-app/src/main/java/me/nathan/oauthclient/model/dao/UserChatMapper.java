package me.nathan.oauthclient.model.dao;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.domain.type.ChatType;
import me.nathan.oauthclient.model.dto.response.api.UserChatDto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UserChatMapper {

    private Long id;

    private String image;

    private String name;

    private String lastMessage;

    private Long lastAt;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    private Long unreadCnt;

    private List<MemberMapper> members;

    @Builder
    public UserChatMapper(Long id, String name, String image, String lastMessage,
                          ChatType type, Long lastAt, Long unreadCnt, List<MemberMapper> memberInfos) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.lastMessage = lastMessage;
        this.type = type;
        this.lastAt = lastAt;
        this.unreadCnt = unreadCnt;
        this.members = memberInfos;
    }

    public UserChatDto toUserChatDto() {
        List<MemberInfo> memberInfos = members
                .stream()
                .map(memberMapper -> MemberInfo.builder()
                        .id(memberMapper.getId())
                        .name(memberMapper.getName())
                        .image(memberMapper.getImage())
                        .active(memberMapper.isActive())
                        .build())
                .collect(Collectors.toList());
        return UserChatDto.builder()
                .id(this.id)
                .image(this.image)
                .name(this.name)
                .type(this.type)
                .lastAt(this.lastAt)
                .lastMessage(this.lastMessage)
                .unreadCnt(this.unreadCnt)
                .members(memberInfos)
                .build();
    }

    public MemberMapper getMember(Long memberId) {
        return members.stream()
                .filter(member -> member.getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(memberId + "  해당 멤버 ID로 참여한 채팅방이 없습니다."));
    }
}
