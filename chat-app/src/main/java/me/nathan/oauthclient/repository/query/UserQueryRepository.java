package me.nathan.oauthclient.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import me.nathan.oauthclient.model.dto.response.api.UserDto;
import me.nathan.oauthclient.model.dao.MemberInfo;
import me.nathan.oauthclient.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserQueryRepository {

    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    public UserQueryRepository(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<UserDto> searchParticipantsUserInfo(Long chatId) {
        QUser user = QUser.user;
        QParticipants participants = QParticipants.participants;

        return jpaQueryFactory
                .select(
                        Projections.bean(UserDto.class,
                                user.id.as("id"), user.name.as("name"), user.profileImage.as("image")))
                .from(user)
                .leftJoin(participants)
                .on(user.id.eq(participants.userId))
                .where(participants.chatId.eq(chatId))
                .orderBy(user.name.asc())
                .fetch();
    }

    /** 유저의 친구 프로필(유저 정보) 조회
     *
     * @param userId : 친구 조회 요청한 USER ID
     * @return : 유저의 친구 USER
     */
    public List<User> findFriends(Long userId) {
        QFriends friends = new QFriends("ff");
        QUser user = new QUser("user");

        return jpaQueryFactory
                .select(user)
                .from(user)
                .leftJoin(friends)
                .on(user.id.eq(friends.friendId))
                .where(friends.userId.eq(userId))
                .orderBy(user.name.asc())
                .fetch();
    }

    /** 채팅에 참여하는 Participants 조회.
     *
     * @param chatId : 채팅 ID
     * @return
     */
    public List<Participants> searchParticipantsByChatId(Long chatId) {
        QParticipants participants = QParticipants.participants;

        return jpaQueryFactory
                .select(participants)
                .from(participants)
                .where(participants.chatId.eq(chatId))
                .fetch();
    }

    /** 채팅에 참여하는 Members 조회. (멤버 프로필)
     *
     * @param chatId : 채팅 ID
     * @return : 채팅에 참여하는 Member 정.
     */
    public List<MemberInfo> searchMembersByChatId(Long chatId) {
        QParticipants participants = QParticipants.participants;
        QUser user = QUser.user;
        return jpaQueryFactory.select(Projections.bean(MemberInfo.class,
                user.id.as("id"), user.name.as("name"), user.profileImage.as("image"), participants.active.as("active")))
                .from(user)
                .leftJoin(participants)
                .on(participants.userId.eq(user.id))
                .where(participants.chatId.eq(chatId))
                .fetch();
    }
}
