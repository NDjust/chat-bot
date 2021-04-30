package me.nathan.oauthclient.domain;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import me.nathan.oauthclient.domain.type.ChatType;
import me.nathan.oauthclient.domain.type.StatusType;

import javax.persistence.*;

@Entity(name = "participants")
@Getter @ToString
public class Participants extends DateTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "active")
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type")
    private ChatType chatType;

    private Long lastOutAt;

    protected Participants() {
    }

    public Participants(Long userId, Long chatId) {
        this.userId = userId;
        this.chatId = chatId;
    }

    @Builder
    public Participants(Long id, Long userId, Long chatId, ChatType chatType, boolean active, Long lastOutAt) {
        this.id = id;
        this.userId = userId;
        this.chatId = chatId;
        this.chatType = chatType;
        this.active = active;
        this.lastOutAt = lastOutAt;
    }

    public void changeStatus(boolean status) {
        this.active = status;
    }
    public void outChat(Long lastOutAt) {
        this.lastOutAt = lastOutAt;
    }
}
