package me.nathan.oauthclient.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.nathan.oauthclient.domain.type.MessageType;

import javax.persistence.*;

@Entity
@Getter
public class Message extends DateTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MessageType type;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    public Message() {
    }

    @Builder
    public Message(Long id, MessageType type, Long chatId, Long userId, String content) {
        this.id = id;
        this.type = type;
        this.chatId= chatId;
        this.userId = userId;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", chatId=" + chatId +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                '}';
    }
}
