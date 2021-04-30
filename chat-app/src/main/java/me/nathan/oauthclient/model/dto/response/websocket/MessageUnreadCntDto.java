package me.nathan.oauthclient.model.dto.response.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageUnreadCntDto implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    private Long id;
    private Integer unreadCnt;
    private Long createdAt;


    public MessageUnreadCntDto(Long id, Integer unreadCnt, Long createdAt) {
        this.id = id;
        this.unreadCnt = unreadCnt;
        this.createdAt = createdAt;
    }

    public boolean isBeforeMessage(long lastEnteredTime) {
        return lastEnteredTime > createdAt;
    }

    public boolean canDecrementUnreadCnt() {
        return unreadCnt > 0;
    }

    public void decrementUnreadCnt() {
        if (unreadCnt > 0) {
            unreadCnt--;
        }
    }
}
