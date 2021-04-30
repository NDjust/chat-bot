package me.nathan.oauthclient.repository;

import lombok.RequiredArgsConstructor;
import me.nathan.oauthclient.domain.Participants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;

@RequiredArgsConstructor
@Repository
public class SocketEventRepository {

    public static final String ENTER_USER_ID = "ENTER_USER_ID";
    public static final String ENTER_CHAT_ID = "ENTER_CHAT_ID";
    public static final String LAST_ENTER_CHAT_ID = "LAST_ENTER_CHAT_ID";

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterUserId;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterChatId;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, Object> lastEnteredChatTime;

    @Autowired
    private ChatRedisRepository chatRedisRepository;

    @Autowired
    private ParticipantsRepository participantsRepository;

    @Autowired
    private ChatMessageUnreadCntRepository messageUnreadCntRepository;

    // 유저 세션으로 입장해 있는 USER ID 조회
    public String getEnterUserId(String sessionId) {
        return hashOpsEnterUserId.get(ENTER_USER_ID, sessionId);
    }

    // 유저 세션으로 입장해 있는 USER ID 조회
    public String getEnterChatId(String sessionId) {
        return hashOpsEnterChatId.get(ENTER_CHAT_ID, sessionId);
    }

    // 유저 세션정보와 맵핑된 CHAT ID 삭제
    private void removeEnterChatId(String sessionId) {
        hashOpsEnterChatId.delete(ENTER_CHAT_ID, sessionId);
    }

    // 유저 세션정보와 맵핑된 USER ID 삭제
    private void removeEnterUserId(String sessionId) {
        hashOpsEnterUserId.delete(ENTER_USER_ID, sessionId);
    }

    // 세션으로 입장한 USER ID 저장.
    public void setEnterUserIdBySessionId(Long userId, String sessionId) {
        hashOpsEnterUserId.put(ENTER_USER_ID, sessionId, String.valueOf(userId));
    }

    // 세션으로 입장한 CHAT ID 저장.
    private void setUserEnterChatId(String sessionId, String chatId) {
        hashOpsEnterChatId.put(ENTER_CHAT_ID, sessionId, chatId);
    }

    // 세션으로 채팅 구독 중인 유저 ID 삭제.
    private void removeUserChatSubScribeUserId(String sessionId) {
        String chatId = getEnterChatId(sessionId);
        String userId = getEnterUserId(sessionId);
        chatRedisRepository.removeChatSubscribeUserId(chatId, userId);
    }

    // 세션으로 채팅 목록 구독 유저 ID 추가.
    public void addSubscribeChatListUser(String sessionId) {
        String userId = getEnterUserId(sessionId);
        chatRedisRepository.addSubscribeChatListUser(Long.parseLong(userId));
    }

    // 세션으로 채팅 목록 구독 중인 USER ID 삭제.
    public void removeSubscribeChatListUser(String sessionId) {
        String userId = getEnterUserId(sessionId);
        String chatId = getEnterUserId(sessionId);
        chatRedisRepository.removeChatListSubscribeUserIdsByChatId(userId, chatId);
    }

    // 세션으로 채팅 구독 중인 USER ID 추가.
    private void addSubscribeChatUserId(String sessionId) {
        String userId = getEnterUserId(sessionId);
        String chatId = getEnterChatId(sessionId);
        chatRedisRepository.addSubscribeChatUserId(chatId, userId);
    }

    // 채팅에 입장한 USER 수 +1
    private void plusChatUserCount(String sessionId) {
        String chatId = getEnterChatId(sessionId);
        chatRedisRepository.plusUserCount(chatId);
    }

    // 채팅에 입장한 USER 수 -1
    private void minusChatUserCount(String sessionId) {
        String chatId = getEnterChatId(sessionId);
        chatRedisRepository.minusUserCount(chatId);
        if (chatRedisRepository.getUserCount(chatId) == 0) {
            messageUnreadCntRepository.removeChatUnreadMessages(chatId);
        }
    }

    // 세션으로 구독 중인 채팅 메세지 데이터 삭제.
    private void removeUserChatUnreadCntMessageDto(String sessionId) {
        String userId = getEnterUserId(sessionId);
        String chatId = getEnterChatId(sessionId);
        chatRedisRepository.removeUserChatUnreadCntMessageDto(chatId, userId);
    }

    // 세션으로 구독 중인 채팅 목록 UnreadCnt 추가.
    private void addChatListUnreadCnt(String sessionId) {
        String userId = getEnterUserId(sessionId);
        String chatId = getEnterChatId(sessionId);
        chatRedisRepository.addUserChatUnreadCnt(userId, chatId, 0);
    }

    private void setLastConnectedChatTime(String sessionId) {
        String userId = getEnterUserId(sessionId);
        String chatId = getEnterChatId(sessionId);
        String key = String.format("%s_%s_%s", LAST_ENTER_CHAT_ID, chatId, userId);
        String outTime = String.valueOf(new Date().getTime());
        lastEnteredChatTime.set(key, outTime);
    }

    public void updateLastEnteredChatTime(String userIdToString, String charIdToString) {
        String key = String.format("%s_%s_%s", LAST_ENTER_CHAT_ID, charIdToString, userIdToString);
        String outTime = String.valueOf(new Date().getTime());
        lastEnteredChatTime.set(key, outTime);
    }

    public String getLastEnteredChatTime(String sessionId) {
        String userId = getEnterUserId(sessionId);
        String chatId = getEnterChatId(sessionId);
        String key = String.format("%s_%s_%s", LAST_ENTER_CHAT_ID, chatId, userId);
        String lastEnteredTime = (String) lastEnteredChatTime.get(key);

        if (lastEnteredTime == null) {
            Participants participants = participantsRepository.findByChatIdAndUserId(Long.parseLong(chatId), Long.parseLong(userId)).orElse(null);
            lastEnteredTime = String.valueOf(participants.getLastAt());
        }

        return lastEnteredTime;
    }

    public String getLastEnteredChatTime(String userId, String chatId) {
        String key = String.format("%s_%s_%s", LAST_ENTER_CHAT_ID, chatId, userId);
        String lastEnteredTime = (String) lastEnteredChatTime.get(key);

        System.out.println("===LastEnteredTime===");
        System.out.println(lastEnteredTime);
        if (lastEnteredTime == null) {
            Participants participants = participantsRepository.findByChatIdAndUserId(Long.parseLong(chatId), Long.parseLong(userId)).orElse(null);
            lastEnteredTime = String.valueOf(participants.getLastAt());
        }

        System.out.println("===LastEnteredTime===");
        System.out.println(lastEnteredTime);

        return lastEnteredTime;
    }

    public void subscribeToChat(String sessionId, String chatId) {
        // 현재 세션(유저 식별)으로 입장한 채팅 저장.
        setUserEnterChatId(sessionId, chatId);

        // 해당 채팅을 구독하는 유저 아이디 저장.
        addSubscribeChatUserId(sessionId);

        // 채팅방의 인원수를 +1한다.
        plusChatUserCount(sessionId);

        addChatListUnreadCnt(sessionId);
    }

    public void disconnectChat(String sessionId) {
        String chatId = getEnterChatId(sessionId);
        String userId = getEnterUserId(sessionId);

        // 해당 채팅 나간 유저의 messageFirstId
        messageUnreadCntRepository.removeUserChatFirstMessageId(chatId, userId);

        setLastConnectedChatTime(sessionId);

        // unreadCntMessage Delete.
        removeUserChatUnreadCntMessageDto(sessionId);

        // 채팅방의 인원수를 -1한다.
        minusChatUserCount(sessionId);

        // 퇴장한 클라이언트의 해당 채팅 구독한 유저 삭제.
        removeUserChatSubScribeUserId(sessionId);

        // 퇴장한 클라이언트의 roomId 맵핑 정보를 삭제한다.
        removeEnterChatId(sessionId);

        // 퇴장한 클라이언트의 userId 맵핑 정보를 삭제한다.
        removeEnterUserId(sessionId);

    }
}
