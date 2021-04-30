package me.nathan.oauthclient.repository.query;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.nathan.oauthclient.domain.Message;
import me.nathan.oauthclient.domain.QConnectedChat;
import me.nathan.oauthclient.domain.QMessage;
import me.nathan.oauthclient.domain.QParticipants;
import me.nathan.oauthclient.domain.type.MessageType;
import me.nathan.oauthclient.model.dto.response.api.ChatMessageUnreadCntDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageQueryRepository {

    private JPAQueryFactory jpaQueryFactory;

    public MessageQueryRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    /** 채팅 내 메세지 조회 쿼리
     *
     *  채팅방에 이전에 나간 기록이 있을시, 나간 이전의 메세지는 제외.
     * @param chatId : 채팅 ID
     * @param userId : 유저 ID
     * @param messageCount : 조회할 메세지 개수
     * @return : messageCount 만큼 조회한 최근 메세지.
     */
    public List<Message> getMessagesByChatIdAndUserId(Long chatId, Long userId, Long messageCount) {
        QMessage message = QMessage.message;
        QParticipants p1 = new QParticipants("p1");
        return jpaQueryFactory.select(message)
                .from(p1)
                .leftJoin(message)
                .on(p1.chatId.eq(message.chatId))
                .where(message.chatId.eq(chatId), p1.userId.eq(userId),
                        message.createdAt.gt(p1.lastOutAt.coalesce(0L)))
                .orderBy(message.createdAt.desc())
                .limit(messageCount)
                .distinct()
                .fetch();
    }

    /**채팅 내 메세지 조회 쿼리
     *
     * - 마지막 메세지 ID 기준으로 이전 20개 조회.
     *
     *  채팅방에 이전에 나간 기록이 있을시, 나간 이전의 메세지는 제외.
     * @param chatId : 채팅 ID
     * @param userId : 유저 ID
     * @param lastMessageId : 마지막 메세지 ID
     * @param messageCount : 메세지 횟수
     * @return
     */
    public List<Message> getMessagesByChatIdAndLastMessageId(Long chatId, Long userId,
                                                             Long lastMessageId, Long messageCount) {
        QMessage message = QMessage.message;
        QParticipants p1 = new QParticipants("p1");
        return jpaQueryFactory.select(message)
                .from(p1)
                .leftJoin(message)
                .on(p1.chatId.eq(message.chatId))
                .where(message.chatId.eq(chatId), p1.userId.eq(userId), message.id.lt(lastMessageId),
                        message.createdAt.gt(p1.lastOutAt.coalesce(0L)))
                .orderBy(message.createdAt.desc())
                .limit(messageCount)
                .distinct()
                .fetch();
    }

    public List<Message> getMessagesByChatId(Long chatId, int messageCount) {
        QMessage message = QMessage.message;

        return jpaQueryFactory.select(message)
                .from(message)
                .where(message.chatId.eq(chatId))
                .orderBy(message.createdAt.desc())
                .limit(messageCount)
                .distinct()
                .fetch();

    }

    public List<ChatMessageUnreadCntDto> getChatMessageUnreadCnt(Long userId) {
        QConnectedChat cc = new QConnectedChat("cc");
        QConnectedChat cc2 = new QConnectedChat("cc2");
        QParticipants pp = new QParticipants("pp");
        QParticipants p = new QParticipants("p");
        QMessage mm = QMessage.message;
        return jpaQueryFactory.select(
                Projections.bean(ChatMessageUnreadCntDto.class, cc.chatId.as("chatId"),
                                ExpressionUtils.as(JPAExpressions.select(mm.id.count())
                                            .from(cc2)
                                                .leftJoin(p).on(p.chatId.eq(cc2.chatId))
                                                .leftJoin(mm).on(mm.chatId.eq(cc2.chatId))
                                            .where(p.userId.eq(userId), cc2.chatId.eq(cc.chatId),
                                                    mm.createdAt.gt(p.lastAt), mm.type.ne(MessageType.NOTI)),
                                        "unreadCnt")))
                .from(cc)
                .where(cc.chatId.in(JPAExpressions.select(pp.chatId)
                                        .from(pp)
                                        .where(pp.active.eq(true), pp.userId.eq(userId))))
                .orderBy(cc.chatId.desc())
                .fetch();
    }
}
