package me.nathan.oauthclient.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.nathan.oauthclient.domain.Chat;
import me.nathan.oauthclient.domain.Participants;
import me.nathan.oauthclient.domain.QChat;
import me.nathan.oauthclient.domain.QParticipants;
import me.nathan.oauthclient.domain.type.ChatType;
import me.nathan.oauthclient.model.dto.response.api.WaitingParticipantChatDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ParticipantsQueryRepository {

    private JPAQueryFactory jpaQueryFactory;

    public ParticipantsQueryRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    /** 아직 초대 받지 않은 참여자 조회.
     *
     * @param friendId : 친구 ID
     * @param userId : 유자 ID
     * @return : WaitingParticipantChatDto
     */
    public WaitingParticipantChatDto getPreviousParticipantChat(Long friendId, Long userId) {
        QParticipants p1 = new QParticipants("p1");
        QParticipants p2 = new QParticipants("p2");
        return jpaQueryFactory.select(Projections.bean(WaitingParticipantChatDto.class,
                p2.userId.as("userId"), p2.chatId.as("chatId")))
                .from(p1)
                .leftJoin(p2)
                .on(p1.chatId.eq(p2.chatId))
                .where(p1.userId.eq(friendId), p2.userId.eq(userId), p1.chatType.eq(ChatType.PRIVATE))
                .fetchOne();
    }

    public Chat getPreviousSelfChat(Long userId) {
        QChat chat = QChat.chat;
        return jpaQueryFactory.select(chat)
                .from(chat)
                .where(chat.creatorId.eq(userId), chat.type.eq(ChatType.SELF))
                .fetchOne();
    }

    public List<Participants> getUserParticipantsOrderByChatId(Long userId) {
        QParticipants participants = QParticipants.participants;

        return jpaQueryFactory
                .select(participants)
                .from(participants)
                .where(participants.userId.eq(userId), participants.active.eq(true))
                .orderBy(participants.chatId.desc())
                .fetch();
    }
}
