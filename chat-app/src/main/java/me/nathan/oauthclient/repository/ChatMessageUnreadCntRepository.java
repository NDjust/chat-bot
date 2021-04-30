package me.nathan.oauthclient.repository;

import lombok.RequiredArgsConstructor;
import me.nathan.oauthclient.model.dto.response.api.MessageDto;
import me.nathan.oauthclient.model.dto.response.websocket.*;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

@RequiredArgsConstructor
@Repository
public class ChatMessageUnreadCntRepository {

    private static final String READ_CHAT_MESSAGES = "READ_CHAT_MESSAGE";
    private static final String CHAT_FIRST_MESSAGE_ID = "CHAT_FIRST_MESSAGE_ID";
    private static final String USER_CHAT_FIRST_MESSAGE_ID = "USER_CHAT_FIRST_MESSAGE_ID";

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "userChatMessageRedisTemplate")
    private ListOperations<String, UnreadCntMessageDto> userChatMessages;

    @Resource(name = "chatMessageRedisTemplate")
    private ListOperations<String, MessageUnreadCntDto> chatMessages;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> chatFirstMessageId;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> userChatFirstMessageId;

    public List<MessageUnreadCntDto> getDecrementUnreadMessages(String chatId, String lastEnteredTime) {
        long lastEnterAt = Long.parseLong(lastEnteredTime);
        String key = String.format("%s_%s", READ_CHAT_MESSAGES, chatId);
        List<MessageUnreadCntDto> messages = Optional.ofNullable(chatMessages.range(key, 0, -1)).orElse(new ArrayList<>());
        List<MessageUnreadCntDto> decrementMessages = new ArrayList<>();

        for (int i = 0; i < messages.size(); i++) {
            MessageUnreadCntDto unreadCntDto = messages.get(i);

            if (!unreadCntDto.isBeforeMessage(lastEnterAt)) {
                unreadCntDto.decrementUnreadCnt();
                decrementMessages.add(unreadCntDto);
                chatMessages.set(key, i, unreadCntDto);
            }
        }
        return decrementMessages;
    }

    // TODO 현재 조회한 메시지가 이전 메시지이면, 앞쪽에 추가.
    //  현재 저장한 메시지 뒤에 메시지이면 뒤에 추가.
    //  firstMessageId update
    public void addChatReadMessages(Long chatId, Long userId, List<MessageDto> messageDtos) {
        String key = String.format("%s_%s", READ_CHAT_MESSAGES, chatId);

        if (messageDtos.isEmpty()) {
            return;
        }

        Long firstMessageId = messageDtos.get(0).getId();
        setUserChatFirstMessageId(chatId, userId, firstMessageId);

        if (!hasChatFirstMessageId(chatId)) {
            setChatFirstMessageId(chatId, firstMessageId);
            addRightExtendMessages(key, messageDtos);
        } else {
            addLeftUnreadMessage(chatId, messageDtos);
            addRightUnreadMessage(chatId, messageDtos);
        }

    }

    private void addRightUnreadMessage(Long chatId, List<MessageDto> messageDtos) {
        String key = String.format("%s_%s", READ_CHAT_MESSAGES, chatId);
        long messageSize = Optional.ofNullable(chatMessages.size(key)).orElse(0L);

        if (messageSize == 0) {
            addRightExtendMessages(key, messageDtos);
            return;
        }

        MessageUnreadCntDto recentlyMessage = chatMessages.range(key, messageSize - 1, -1).get(0);
        Long recentlyMessageId = recentlyMessage.getId();

        for (int i = 0; i < messageDtos.size(); i++) {
            MessageDto messageDto = messageDtos.get(0);

            if (messageDto.getId() > recentlyMessageId && messageDto.getUnreadCnt() > 0) {
                chatMessages.rightPush(key, messageDto.toMessageUnreadCntDto());
            }
        }
    }

    private void addRightExtendMessages(String key, List<MessageDto> messageDtos) {
        for (MessageDto messageDto : messageDtos) {
            if (messageDto.getUnreadCnt() > 0) {
                chatMessages.rightPush(key, messageDto.toMessageUnreadCntDto());
            }
        }
    }

    private void addLeftUnreadMessage(Long chatId, List<MessageDto> messageDtos) {
        String key = String.format("%s_%s", READ_CHAT_MESSAGES, chatId);
        Long chatFirstMessageId = getChatFirstMessageId(chatId);
        Long newMessageId = messageDtos.get(0).getId();
        int idx = 0;
        if (newMessageId < chatFirstMessageId) {
            setChatFirstMessageId(chatId, newMessageId);
            List<MessageUnreadCntDto> unreadCntDtos = new ArrayList<>();

            while (newMessageId < chatFirstMessageId) {
                MessageUnreadCntDto unreadCntDto = messageDtos.get(idx).toMessageUnreadCntDto();

                if (unreadCntDto.getUnreadCnt() > 0) {
                    unreadCntDtos.add(unreadCntDto);
                }

                idx++;

                if (idx >= messageDtos.size()) {
                    break;
                }

                newMessageId = messageDtos.get(idx).getId();
            }

            addLeftExtendMessages(key, unreadCntDtos);
        }

    }

    private void addLeftExtendMessages(String key, List<MessageUnreadCntDto> unreadCntDtos) {
        unreadCntDtos.sort(new Comparator<MessageUnreadCntDto>() {
            @Override
            public int compare(MessageUnreadCntDto o1, MessageUnreadCntDto o2) {
                return Long.compare(o2.getId(), o1.getId());
            }
        });

        unreadCntDtos.forEach(unreadCntDto -> chatMessages.leftPush(key, unreadCntDto));
    }

    public void addChatReadMessage(Long chatId, NewMessageDto newMessageDto) {
        String key = String.format("%s_%s", READ_CHAT_MESSAGES, chatId);
        chatMessages.rightPush(key, newMessageDto.toMessageUnreadCntDto());
    }

    private void setUserChatFirstMessageId(Long chatId, Long userId, Long firstMessageId) {
        String key = String.format("%s_%s_%s", USER_CHAT_FIRST_MESSAGE_ID, chatId, userId);
        long previousId = Long.parseLong(Optional.ofNullable(userChatFirstMessageId.get(key)).orElse("0"));

        if (previousId == 0) {
            userChatFirstMessageId.set(key, String.valueOf(firstMessageId));
        } else {
            if (previousId > firstMessageId) {
                userChatFirstMessageId.set(key, String.valueOf(firstMessageId));
            }
        }

    }

    public String getUserChatFirstMessageId(Long chatId, Long userId) {
        String key = String.format("%s_%s_%s", USER_CHAT_FIRST_MESSAGE_ID, chatId, userId);
        return userChatFirstMessageId.get(key);
    }

    public String getUserChatFirstMessageId(String chatId, String userId) {
        String key = String.format("%s_%s_%s", USER_CHAT_FIRST_MESSAGE_ID, chatId, userId);
        return Optional.ofNullable(userChatFirstMessageId.get(key)).orElse("0");
    }

    public void removeUserChatFirstMessageId(String  chatId, String  userId) {
        String key = String.format("%s_%s_%s", USER_CHAT_FIRST_MESSAGE_ID, chatId, userId);
        redisTemplate.delete(key);
    }

    public Long getChatFirstMessageId(Long chatId) {
        return Long.parseLong(
                Optional.ofNullable(chatFirstMessageId.get(CHAT_FIRST_MESSAGE_ID + "_" + chatId)).orElse("0"));
    }

    private void setChatFirstMessageId(Long chatId, Long firstMessageId) {
        chatFirstMessageId.set(CHAT_FIRST_MESSAGE_ID + "_" + chatId, String.valueOf(firstMessageId));
    }

    public Boolean hasChatFirstMessageId(Long chatId) {
        return redisTemplate.hasKey(CHAT_FIRST_MESSAGE_ID + "_" + chatId);
    }

    private void deleteChatFirstMessageId(String chatId) {
        redisTemplate.delete(CHAT_FIRST_MESSAGE_ID + "_" + chatId);
    }

    public void removeChatUnreadMessages(String chatId) {
        String key = String.format("%s_%s", READ_CHAT_MESSAGES, chatId);
        deleteChatFirstMessageId(chatId);
        redisTemplate.delete(key);
    }
}
