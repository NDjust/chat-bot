package me.nathan.oauthclient.domain;

import lombok.Builder;
import lombok.Getter;
import me.nathan.oauthclient.domain.type.ChatType;

import javax.persistence.*;

@Entity
@Getter
public class Chat extends DateTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "image")
    private String image;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ChatType type;

    @Column(name = "status")
    private String status;

    public Chat() {
    }

    @Builder
    public Chat(Long id, String name, String image, Long creatorId, ChatType type, String status) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.creatorId = creatorId;
        this.type = type;
    }
}
