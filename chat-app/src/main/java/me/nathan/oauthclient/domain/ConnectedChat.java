package me.nathan.oauthclient.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "connected_chat")
@Getter @Setter
public class ConnectedChat extends DateTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    protected ConnectedChat() {
    }

    @Builder
    public ConnectedChat(Long id, Long chatId, Long lastMessageId) {
        this.id = id;
        this.chatId = chatId;
        this.lastMessageId = lastMessageId;
    }
}
