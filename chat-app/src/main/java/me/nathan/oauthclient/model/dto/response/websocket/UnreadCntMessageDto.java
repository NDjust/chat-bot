package me.nathan.oauthclient.model.dto.response.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnreadCntMessageDto implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    private Long id;
    private Set<Long> unreadCntUserIds;

    public UnreadCntMessageDto(Long id, Set<Long> unreadCntUserIds) {
        this.id = id;
        this.unreadCntUserIds = unreadCntUserIds;
    }

    public void removeUnreadCntUserId(Long userId) {
        unreadCntUserIds.remove(userId);
    }

    public Long getId() {
        return id;
    }

    public Set<Long> getUnreadCntUserIds() {
        return unreadCntUserIds;
    }

    @Override
    public String toString() {
        return "UnreadCntMessageDto{" +
                "id=" + id +
                ", unreadCntUserIds=" + unreadCntUserIds.toString() +
                '}';
    }
}
