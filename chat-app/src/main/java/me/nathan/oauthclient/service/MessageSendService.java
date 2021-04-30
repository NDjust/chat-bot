package me.nathan.oauthclient.service;

import lombok.RequiredArgsConstructor;
import me.nathan.oauthclient.domain.Message;
import me.nathan.oauthclient.domain.type.ChatType;
import me.nathan.oauthclient.model.dao.MemberInfo;
import me.nathan.oauthclient.model.dto.response.api.ChatDto;
import me.nathan.oauthclient.model.dto.response.websocket.*;
import me.nathan.oauthclient.model.type.ChatListType;
import me.nathan.oauthclient.model.type.ChatMessageType;
import me.nathan.oauthclient.repository.ChatMessageUnreadCntRepository;
import me.nathan.oauthclient.repository.ChatRedisRepository;
import me.nathan.oauthclient.repository.ParticipantsRepository;
import me.nathan.oauthclient.repository.SocketEventRepository;
import me.nathan.oauthclient.util.common.SubscribeTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageSendService {

    @Autowired
    private ChatRedisRepository chatRedisRepository;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private SocketEventRepository socketEventRepository;

    @Autowired
    private ChatMessageUnreadCntRepository messageUnreadCntRepository;

    /** 개인 톡 메세지.
     *
     * - 현재 채팅을 구독 중인 유저에 해당 메세지와 unreadUserIds 전송.
     * - 채팅 목록 구독 중인 유저들에 unreadCnt +1 증가.
     * @param senderId : 보낸 USER ID
     * @param chatId : 채팅 ID
     * @param saveMessage : 메세지
     * @param unreadUserIds : 메세지 안읽은 USER IDS
     */
    public void convertAndSendPrivateChatMessage(Long senderId, Long chatId, Message saveMessage, Set<Long> unreadUserIds) {
        NewMessageDto newMessageDto = getNewMessageDto(saveMessage, unreadUserIds.size());

        String destination = String.format("%s/%s", SubscribeTopic.MESSAGE_TOPIC, chatId);

        messagingTemplate.convertAndSend(destination, newMessageDto);
        messageUnreadCntRepository.addChatReadMessage(chatId, newMessageDto);
        chatRedisRepository.incrementUsersChatListUnreadCnt(senderId, chatId);
    }

    /** 그룹 톡 메세지.
     *
     * - 현재 채팅을 구독 중인 유저에 해당 메세지와 unreadUserIds 전송.
     * - 채팅 목록 구독 중인 유저들에 unreadCnt +1 증가.
     * @param senderId : 보낸 USER ID
     * @param chatId : 채팅 ID
     * @param saveMessage : 메세지
     * @param unreadCntIds : 메세지 안읽은 USER IDS
     */
    public void convertAndSendGroupChatMessage(Long senderId, Long chatId, Message saveMessage, Set<Long> unreadCntIds) {
        NewMessageDto newMessageDto = getNewMessageDto(saveMessage, unreadCntIds.size());

        String destination = String.format("%s/%s", SubscribeTopic.MESSAGE_TOPIC, chatId);

        messagingTemplate.convertAndSend(destination, newMessageDto);
        messageUnreadCntRepository.addChatReadMessage(chatId, newMessageDto);
        chatRedisRepository.incrementUsersChatListUnreadCnt(senderId, chatId);
    }

    /** 초대 메세지.
     *
     * - 현재 채팅을 구독 중인 유저에 해당 메세지와 unreadUserIds 전송.
     * - 채팅 목록 구독 중인 유저들에 unreadCnt +1 증가.
     * @param senderId : 보낸 USER ID
     * @param chatId : 채팅 ID
     * @param saveMessage : 메세지
     * @param invitedIds : 초대된 USER IDS
     */
    public void convertAndInitGroupChatMessage(Long senderId, Long chatId, Message saveMessage, Set<Long> invitedIds) {
        NewMessageDto newMessageDto = getNewMessageDto(saveMessage, 0);

        String destination = String.format("%s/%s", SubscribeTopic.MESSAGE_TOPIC, chatId);
        messagingTemplate.convertAndSend(destination, newMessageDto);
        chatRedisRepository.addUserGroupChatReadMessage(chatId, newMessageDto, invitedIds);
        chatRedisRepository.incrementUsersChatListUnreadCnt(senderId, chatId);
    }


    public void convertAndSendSelfChatMessage(Long chatId, Message saveMessage) {
        NewMessageDto newMessageDto = getNewMessageDto(saveMessage, 0);
        String destination = String.format("%s/%s", SubscribeTopic.MESSAGE_TOPIC, chatId);
        messagingTemplate.convertAndSend(destination, newMessageDto);
    }

    /** 특정 채팅을 목록에서 새롭게 추가된 구독자에게 새롭게 추가된 메세지 전달.
     *
     *
     * @param saveMessage : 새롭게 추가된 메세지
     * @param newChatParticipants : 새롭게 채팅에 참여할 participants User Ids
     * @param chatType : 채팅 타입     */
    public void sendNewChatListMessage(Message saveMessage, List<MemberInfo> memberInfos,
                                       Set<Long> newChatParticipants, ChatType chatType) {
        for (Long participantUserId : newChatParticipants) {
            String destination = String.format("%s/%s", SubscribeTopic.CHAT_TOPIC, participantUserId);
            Long chatId = saveMessage.getChatId();
            ChatDto chatDto = ChatDto.builder()
                    .id(chatId)
                    .name(null)
                    .image(null)
                    .lastMessage(saveMessage.getContent())
                    .lastAt(saveMessage.getCreatedAt())
                    .type(chatType)
                    .members(memberInfos)
                    .unreadCnt(1L)
                    .build();

            NewChatListMessageDto newChatListMessageDto = new NewChatListMessageDto(ChatListType.NEW, chatDto);
            messagingTemplate.convertAndSend(destination, newChatListMessageDto);
            chatRedisRepository.addSubscribeChatListByWebSocket(chatId, participantUserId);
        }
    }

    /** 특정 채팅을 목록에서 구독중인 구독자에게 새롭게 추가된 메세지 전달.
     *
     *
     * @param chatId : 채팅 ID
     * @param saveMessage : 새롭게 추가된 메세지
     * @param existChatParticipants : 이미 채팅에 참여중인 participants User Ids
     */
    public void sendExistChatListMessage(Long chatId, Message saveMessage, Set<Long> existChatParticipants) {
        Map<String, String> usersChatUnreadCnt = chatRedisRepository.getUsersChatUnreadCnt(chatId);

        for (String userId : usersChatUnreadCnt.keySet()) {
            String destination = String.format("%s/%s", SubscribeTopic.CHAT_TOPIC, userId);
            String userUnreadCnt = usersChatUnreadCnt.get(userId);

            if (existChatParticipants.contains(Long.parseLong(userId))) {
                ChatListMessageDto messageDto = ChatListMessageDto.builder()
                        .type(ChatListType.EXIST)
                        .id(chatId)
                        .lastMessage(saveMessage.getContent())
                        .lastAt(saveMessage.getLastAt())
                        .unreadCnt(Long.parseLong(userUnreadCnt))
                        .build();

                messagingTemplate.convertAndSend(destination, messageDto);
            }
        }

    }

    /** 채팅 나가기.
     *
     *  채팅 나가기 요 시, 해당 채팅을 구독 중인 모든 유저의 메세지 UnreadCnt -1 씩 감소.
     * @param userId : 유저 ID
     * @param chatId : 나간 채팅 ID
     */
    public void outUserChat(Long userId, Long chatId) {
        String userIdToString = String.valueOf(userId);
        String charIdToString = String.valueOf(chatId);
        String lastEnteredAt = socketEventRepository.getLastEnteredChatTime(userIdToString, charIdToString);

        chatRedisRepository.updateUserChatUnreadCnt(userId, chatId, 0L);
        sendAndUpdateUnreadCnt(userIdToString, charIdToString, lastEnteredAt);
    }


    /** 채팅 읽음.
     *
     *  채팅 읽음 요청 시, 해당 채팅을 구독 중인 모든 유저의 메세지 UnreadCnt -1 씩 감소.
     * @param userId : 유저 ID
     * @param chatId : 읽은 채팅 ID
     */
    public void readUserChat(Long userId, Long chatId) {
        String userIdToString = String.valueOf(userId);
        String charIdToString = String.valueOf(chatId);
        String lastEnteredAt = socketEventRepository.getLastEnteredChatTime(userIdToString, charIdToString);

        chatRedisRepository.updateUserChatUnreadCnt(userId, chatId, 0L);
        socketEventRepository.updateLastEnteredChatTime(userIdToString, charIdToString);
        sendAndUpdateUnreadCnt(userIdToString, charIdToString, lastEnteredAt);
    }

    /** 그룹 채팅 나간 유저 알림 메시지.
     *
     *  나감 NOTI 메시지와 나간 유저 ID.
     * @param chatId : 채팅 ID
     * @param outUserId : 나간 유저 ID
     * @param outMessage : 나감 NOTI 메세지
     */
    public void sendGroupChatOutUser(Long chatId, Long outUserId, Message outMessage) {
        ChatOutUserDto chatOutUserDto = new ChatOutUserDto(ChatMessageType.EXIT, outUserId);
        String messageDestination = String.format("%s/%s", SubscribeTopic.MESSAGE_TOPIC, chatId);
        NewMessageDto outNotiMessage = getNewMessageDto(outMessage, 0);

        messagingTemplate.convertAndSend(messageDestination, outNotiMessage);
        messagingTemplate.convertAndSend(messageDestination, chatOutUserDto);
    }

    /** 각 채팅 메세지들의 unreadCnt 1씩 감소.
     *
     *  - 채팅방의 유저가 한명 나갔거나 읽음 처리 시 이벤트 처리.
     * @param chatId : 채팅 id
     */
    public void sendAndUpdateUnreadCnt(String enterUserId, String chatId, String lastConnectedChatTime) {
        List<MessageUnreadCntDto> unreadMessages = messageUnreadCntRepository.getDecrementUnreadMessages(chatId, lastConnectedChatTime);
        Set<String> userIds = chatRedisRepository.getSubscribeChatUserIds(chatId);
        userIds.remove(enterUserId);

        if (userIds.isEmpty() || unreadMessages.isEmpty()) {
            return;
        }

        for (String userId : userIds) {
            String destination = String.format("%s/%s/%s", SubscribeTopic.UNREAD_COUNT_TOPIC, chatId, userId);
            long firstMessageId = Long.parseLong(messageUnreadCntRepository.getUserChatFirstMessageId(chatId, userId));
            long chatMessageId = unreadMessages.get(0).getId();
            int idx = 0;

            while (firstMessageId > chatMessageId) {
                chatMessageId = unreadMessages.get(++idx).getId();
            }

            List<MessageUnreadCntDto> unreadCntDtos = unreadMessages.subList(idx, unreadMessages.size());
            messagingTemplate.convertAndSend(destination,UnreadCntMessageDtos.of(ChatMessageType.UNREADCNT, unreadCntDtos));
        }
    }

    private NewMessageDto getNewMessageDto(Message saveMessage, int unreadCnt) {
        return NewMessageDto.builder()
                .chatMessageType(ChatMessageType.NEWMESSAGE)
                .type(saveMessage.getType())
                .id(saveMessage.getId())
                .userId(saveMessage.getUserId())
                .content(saveMessage.getContent())
                .sentAt(saveMessage.getCreatedAt())
                .unreadCnt((long) unreadCnt)
                .build();
    }

}
