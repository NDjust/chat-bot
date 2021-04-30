package me.nathan.oauthclient.repository;

import lombok.RequiredArgsConstructor;
import me.nathan.oauthclient.domain.Participants;
import me.nathan.oauthclient.model.dto.response.api.MessageDto;
import me.nathan.oauthclient.model.dto.response.websocket.NewMessageDto;
import me.nathan.oauthclient.model.dto.response.websocket.UnreadCntMessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;


@RequiredArgsConstructor
@Repository
public class ChatRedisRepository {

    private static final String CHAT = "CHAT";
    private static final String USER_COUNT = "USER_COUNT";
    private static final String SUBSCRIBE_CHAT_LIST_CHAT_ID = "CHAT_LIST";
    private static final String READ_MESSAGE = "READ_MESSAGE";

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "redisTemplate")
    private SetOperations<String, String> setOpsSubscribeChatListUserIdsByChatId;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, Object> valueOpsSubscribeChatUserCount;

    @Resource(name = "redisTemplate")
    private SetOperations<String, String> setOpsSubscribeChatUserIds;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsUserChatListUnreadCnt;

    @Resource(name = "userChatMessageRedisTemplate")
    private ListOperations<String, UnreadCntMessageDto> listOperationUserChatMessages;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ParticipantsRepository participantsRepository;


    /** 채팅 리스트 구독 이벤트.
     *
     *  유저가 채팅 리스트 구독 시, 해당 유저의 모든 CHAT ID & USER ID 레디스에 저장.
     *  Key : CHAT_LIST_{CHAT_ID} Value : USER ID
     * @param userId : 채팅 리스트 구독한 USER ID
     */
    public void addSubscribeChatListUser(Long userId) {
        List<Participants> participants = participantsRepository.findByUserIdAndActive(userId, true)
                        .orElse(new ArrayList<>());

        if (participants.size() == 0) {
            return;
        }

        for (Participants participant : participants) {
            String key = SUBSCRIBE_CHAT_LIST_CHAT_ID + "_" + participant.getChatId();
            setOpsSubscribeChatListUserIdsByChatId.add(key, String.valueOf(userId));
        }
    }

    public void addSubscribeChatListByWebSocket(Long chatId, Long participantUserId) {
        String key = SUBSCRIBE_CHAT_LIST_CHAT_ID + "_" + chatId;
        setOpsSubscribeChatListUserIdsByChatId.add(key, String.valueOf(participantUserId));
        addUserChatUnreadCnt(participantUserId, chatId, 1L);
    }

    /** 특정 채팅을 채팅 목록 토팍에서 구독 중인 모든 USER ID 조회.
     *
     *  메세지 전송 이벤트 발생시 토픽에 생성된 메세지 전달하기 위함.
     * @param chatId : 채팅 ID
     * @return
     */
    public Set<String> getChatListSubscribeUserIdsByChatId(Long chatId) {
        String key = SUBSCRIBE_CHAT_LIST_CHAT_ID + "_" + chatId;
        return setOpsSubscribeChatListUserIdsByChatId.members(key);
    }

    /**  채팅 목록 데이터 삭제.
     *
     * 채팅 목록 구독 DISCONNECT 시, 해당 채팅 목록 구독 데이터 삭제.
     *
     * @param chatId : 채팅 ID
     * @param userId : 유저 ID
     */
    public void removeChatListSubscribeUserIdsByChatId(String chatId, String userId) {
        String key = SUBSCRIBE_CHAT_LIST_CHAT_ID + "_" + chatId;
        setOpsSubscribeChatListUserIdsByChatId.remove(key, 1, userId);
    }

    // 채팅방 유저수 조회
    public long getUserCount(String chatId) {
        return Long.parseLong(String.valueOf((Integer) Optional.ofNullable(valueOpsSubscribeChatUserCount.get(USER_COUNT + "_" + chatId)).orElse((String) "0")));
    }

    // 채팅방에 입장한 유저수 +1
    public long plusUserCount(String chatId) {
        return Optional.ofNullable(valueOpsSubscribeChatUserCount.increment(USER_COUNT + "_" + chatId)).orElse(0L);
    }

    // 채팅방에 입장한 유저수 -1
    public long minusUserCount(String chatId) {
        return Optional.ofNullable(valueOpsSubscribeChatUserCount.decrement(USER_COUNT + "_" + chatId)).filter(count -> count > 0).orElse(0L);
    }

    /** 특정 채팅 방을 구독하는 USER ID 저장.
     *
     *  Key : CHAT_{CHAT_ID} Value : USER ID
     * @param chatId : 채팅 ID
     * @param userId : 유저 ID
     */
    public void addSubscribeChatUserId(String chatId, String userId) {
        setOpsSubscribeChatUserIds.add(CHAT + "_" + chatId, String.valueOf(userId));
    }

    /** 채팅방 구독 취소 시, 삭제.
     *
     * @param chatId : 채팅 ID
     * @param userId : 유저 ID
     */
    public void removeChatSubscribeUserId(String chatId, String userId) {
        setOpsSubscribeChatUserIds.remove(CHAT + "_" + chatId, userId);
    }

    /** 특정 채팅을 구독하고 있는 모든 유저 ID 조회.
     *
     * @param chatId : 채팅 ID
     * @return
     */
    public Set<String> getSubscribeChatUserIds(String chatId) {
        return setOpsSubscribeChatUserIds.members(CHAT + "_" + chatId);
    }

    public boolean hasChatUserId(String chatId, String userId) {
        return setOpsSubscribeChatUserIds.isMember(CHAT + "_" + chatId, userId);
    }

    /** 채팅 목록 Subscriber UnreadCnt
     * [subscribe] /sub/chat/list/{userId}  // 목록 페이지
     *
     * 유저의 채팅목록에서 갱신되는 UnreadCnt 추가.
     * @param userId : 구독중인 유저.
     * @param chatId : 구독중인 채팅방.
     * @param unreadCnt : 해당 유저의 채팅방 unreadCnt
     */
    public void addUserChatUnreadCnt(Long userId, Long chatId, Long unreadCnt) {
        String chatIdByString = String.valueOf(chatId);
        String usrIdByString = String.valueOf(userId);
        hashOpsUserChatListUnreadCnt.put(chatIdByString, usrIdByString, String.valueOf(unreadCnt));
    }

    public void addUserChatUnreadCnt(String userId, String chatId, Integer unreadCnt) {
        hashOpsUserChatListUnreadCnt.put(chatId, userId, String.valueOf(unreadCnt));
    }

    public void updateUserChatUnreadCnt(Long userId, Long chatId, long unreadCnt) {
        String chatIdByString = String.valueOf(chatId);
        String usrIdByString = String.valueOf(userId);
        hashOpsUserChatListUnreadCnt.put(chatIdByString, usrIdByString, String.valueOf(unreadCnt));
    }

    /** 채팅 목록 unreadCnt 증가.
     *
     * 채팅에 새롭게 메세지가 추가시 구독중인 유저들의 채팅 목록 unreadCnt 증가.
     *가
     * @param senderId : 메세지를 보낸 유저 id.
     * @param chatId : 채팅 id.
     */
    public void incrementUsersChatListUnreadCnt(Long senderId, Long chatId) {
        String key = Long.toString(chatId);
        Map<String, String> entries = hashOpsUserChatListUnreadCnt.entries(key);

        for (String userId : entries.keySet()) {
            if (!userId.equals(String.valueOf(senderId))) {
                long value = Long.parseLong(Optional.ofNullable(hashOpsUserChatListUnreadCnt.get(key, userId)).orElse("0"));
                hashOpsUserChatListUnreadCnt.put(key, userId, String.valueOf(value + 1));
            }
        }
    }

    /** 특정 채팅 id을 구독중인 유저들의 현재 unreadCnt 조회.
     *
     * @param chatId : 채팅 id
     * @return : 유저 id : unreadCnt Map.
     */
    public Map<String, String> getUsersChatUnreadCnt(Long chatId) {
        String key = String.valueOf(chatId);
        return hashOpsUserChatListUnreadCnt.entries(key);
    }

    public void removeUserChatUnreadCnt(Long userId, Long chatId) {
        hashOpsUserChatListUnreadCnt.delete(String.valueOf(userId), String.valueOf(chatId));
    }

    /** 유저가 채팅 내 메세지 조회 시, 각 메세지에 대한 데이터 저장.
     *
     * @param chatId : 채팅 id
     * @param userId : 유저 id
     * @param messages : 현재 조회한 채팅 내 메세지.
     */
    public void addUserChatReadMessages(Long chatId, Long userId, List<MessageDto> messages) {
        String key = String.format("%s_%s_%s", READ_MESSAGE, chatId, userId);

        for (MessageDto message : messages) {
            listOperationUserChatMessages.rightPush(key,
                    new UnreadCntMessageDto(message.getId(), message.getUnreadCntUserIds()));
        }
    }

    /** 채팅방 메세지 생성시, 해당 메세지 레디스 추가.
     *
     *  해당 채팅을 구독하고 있는 모든 유저 Redis 데이터에 추가.
     * @param chatId : 채팅 id
     * @param newMessageDto : 새롭게 추가된 메세지 Dto.
     * @param unreadUserIds : 해당 메세지의 UnreadCnt User Ids.
     */
    public void addUserChatReadMessage(Long chatId, NewMessageDto newMessageDto, Set<Long> unreadUserIds) {
        Set<String> userIds = getSubscribeChatUserIds(String.valueOf(chatId));

        for (String userId : userIds) {
            String key = String.format("%s_%s_%s", READ_MESSAGE, chatId, userId);
            listOperationUserChatMessages.rightPush
                    (key, new UnreadCntMessageDto(newMessageDto.getMessage().getId(),  unreadUserIds));
        }
    }


    public void addUserGroupChatReadMessage(Long chatId, NewMessageDto newMessageDto, Set<Long> unreadUserIds) {
        Set<String> userIds = getSubscribeChatUserIds(String.valueOf(chatId));

        for (String userId : userIds) {
            String key = String.format("%s_%s_%s", READ_MESSAGE, chatId, userId);

            listOperationUserChatMessages.rightPush(
                    key, new UnreadCntMessageDto(newMessageDto.getMessage().getId(), unreadUserIds));
        }
    }

    /** 특정 채팅 & 유저의 현재까지 보고 있는 메세지 데이터 제거.
     *
     *  채팅방 구독 종료시.
     * @param chatId : 채팅 id
     * @param userId : 유저 id
     */
    public void removeUserChatUnreadCntMessageDto(String chatId, String userId) {
        String key = String.format("%s_%s_%s", READ_MESSAGE, chatId, userId);
        redisTemplate.delete(key);
    }
}
