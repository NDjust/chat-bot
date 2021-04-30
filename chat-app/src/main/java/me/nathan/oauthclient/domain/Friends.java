package me.nathan.oauthclient.domain;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Entity(name = "friends")
@Getter
public class Friends extends DateTimeEntity {

    @Id @GeneratedValue
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "friend_id")
    private Long friendId;

    private String status;

    protected Friends() {
    }

    @Builder
    public Friends(Long id, Long userId, Long friendId, String status) {
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }
}
