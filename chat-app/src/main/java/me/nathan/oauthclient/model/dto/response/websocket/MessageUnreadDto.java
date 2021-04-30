package me.nathan.oauthclient.model.dto.response.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageUnreadDto {

    private Long id;

    private Integer unreadCnt;

    public MessageUnreadDto(Long id, Integer unreadCnt) {
        this.id = id;
        this.unreadCnt = unreadCnt;
    }
}
