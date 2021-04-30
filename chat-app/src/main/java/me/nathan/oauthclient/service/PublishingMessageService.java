package me.nathan.oauthclient.service;

import lombok.NoArgsConstructor;
import me.nathan.oauthclient.domain.ConnectedChat;
import me.nathan.oauthclient.domain.Message;
import me.nathan.oauthclient.domain.Participants;
import me.nathan.oauthclient.domain.type.ChatType;
import me.nathan.oauthclient.domain.type.MessageType;
import me.nathan.oauthclient.model.dao.MemberInfo;
import me.nathan.oauthclient.model.dto.request.websocket.ChatMessageDto;
import me.nathan.oauthclient.model.dto.request.websocket.JoinChatDto;
import me.nathan.oauthclient.model.dto.response.api.UserDto;
import me.nathan.oauthclient.model.type.ChatListType;
import me.nathan.oauthclient.repository.*;
import me.nathan.oauthclient.repository.query.MessageQueryRepository;
import me.nathan.oauthclient.repository.query.UserQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.IntStream;

@Service
@NoArgsConstructor
public class PublishingMessageService {

    @Autowired
    private ParticipantsRepository participantsRepository;

    @Autowired
    private MessageSendService messageSendService;

    @Autowired
    private ChatRedisRepository chatRedisRepository;

    @Autowired
    private SocketEventRepository socketEventRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConnectedChatRepository connectedChatRepository;

    @Autowired
    private UserQueryRepository userQueryRepository;

    @Autowired
    private MessageQueryRepository messageQueryRepository;

    public void sendPrivateMessage(String sessionId, ChatMessageDto messageDto) {
        Long userId = Long.parseLong(socketEventRepository.getEnterUserId(sessionId));
        Long chatId = messageDto.getChatId();

        Message message = saveMessage(userId, messageDto);

        Map<ChatListType, Set<Long>> participantsByChatListType = updateChatParticipantsActive(chatId);
        Set<Long> unreadUserIds = getUnreadUserIds(message, chatId);
        List<MemberInfo> memberInfos = userQueryRepository.searchMembersByChatId(chatId);

        messageSendService.convertAndSendPrivateChatMessage(userId, chatId, message, unreadUserIds);
        messageSendService.sendNewChatListMessage(message, memberInfos, participantsByChatListType.get(ChatListType.NEW), ChatType.PRIVATE);
        messageSendService.sendExistChatListMessage(chatId, message, participantsByChatListType.get(ChatListType.EXIST));
    }

    /** 그룹 채팅방 구독 중인 유저에게 메세지 전달.
     *
     * @param sessionId : 접속한 세션 ID
     * @param messageDto : 보낸 메세지 Dto
     */
    public void sendGroupMessage(String sessionId, ChatMessageDto messageDto) {
        Long userId = Long.parseLong(socketEventRepository.getEnterUserId(sessionId));
        Long chatId = messageDto.getChatId();
        boolean isInvitedMessage = isInitGroupChatMessage(chatId);

        Message message = saveMessage(userId, messageDto);
        Set<Long> unreadUserIds = getUnreadUserIds(message, chatId);

        messageSendService.convertAndSendGroupChatMessage(userId, chatId, message, unreadUserIds);
        if (isInvitedMessage) {
            List<MemberInfo> memberInfos = userQueryRepository.searchMembersByChatId(chatId);
            messageSendService.sendNewChatListMessage(message, memberInfos, unreadUserIds, ChatType.GROUP);
        } else {
            messageSendService.sendExistChatListMessage(chatId, message, unreadUserIds);
        }
    }

    public void sendGroupInvitedMessage(String sessionId, JoinChatDto joinDto) {
        List<Long> invitedIds = joinDto.getInvitedIds();
        Long chatId = joinDto.getChatId();
        Long userId = Long.parseLong(socketEventRepository.getEnterUserId(sessionId));

        Message message = saveJoinMessage(userId, chatId);

        updateChatParticipantsActive(chatId);
        messageSendService.convertAndInitGroupChatMessage(userId, chatId, message, new HashSet<>(invitedIds));
    }

    public void sendSelfMessage(String sessionId, ChatMessageDto messageDto) {
        Long userId = Long.parseLong(socketEventRepository.getEnterUserId(sessionId));
        Long chatId = messageDto.getChatId();

        Message message = saveMessage(userId, messageDto);
        messageSendService.convertAndSendSelfChatMessage(chatId, message);
    }

    /** 채팅에서 보낸 메시지
     *
     * - 새롭게 저장된 메시지마다 Connected Chat Last Message ID 갱신.
     * - 보낸 유저 채팅 마지막 참여 시점 갱신.
     * @param senderId : 메시지 보낸 유저 ID
     * @param message : 보낸 Message Dto
     * @return : 저장된 메시지
     */
    @Transactional
    public Message saveMessage(Long senderId, ChatMessageDto message) {
        Message newMessage = Message.builder()
                .type(message.getType())
                .userId(senderId)
                .chatId(message.getChatId())
                .content(message.getContent())
                .build();

        newMessage = messageRepository.save(newMessage);
        updateConnectedChat(message, newMessage);

        return newMessage;
    }

    @Transactional
    private void updateConnectedChat(ChatMessageDto message, Message newMessage) {
        ConnectedChat connectedChat = connectedChatRepository.findByChatId(message.getChatId())
                .orElse(null);
        if (connectedChat == null) {
            connectedChat = ConnectedChat.builder()
                    .lastMessageId(newMessage.getId())
                    .chatId(newMessage.getChatId())
                    .build();
            connectedChatRepository.save(connectedChat);
        } else {
            long lastAt = new Date().getTime();
            connectedChatRepository.updateLastAtAndLastMessageId(newMessage.getChatId(), lastAt, newMessage.getId());
        }
    }


    /** 새롭게 생성된 채팅을 아직 읽지 않은 User-IDs
     *
     * - 디비 기준 해당 메세지 읽지 않은 유저에서 현재 채팅 참여자 뺀 User Ids
     * @param saveMessage : 새롭게 저장된 메세지
     * @param chatId : 저장된 Chat ID
     * @return : 유저 Ids
     */
    private Set<Long> getUnreadUserIds(Message saveMessage, Long chatId) {
        Set<Long> unreadUserIds = getUnreadUserIds(saveMessage);
        Set<String> subscribeChatUserIds = chatRedisRepository.getSubscribeChatUserIds(String.valueOf(chatId));

        for (String subscribeChatUserId : subscribeChatUserIds) {
            unreadUserIds.remove(Long.parseLong(subscribeChatUserId));
        }

        return unreadUserIds;
    }


    /** 채팅 새롭게 초대되는 Participants 활성화,
     *
     *  새롭게 초대되는 Participants와 이미 초대된 유저 구분.
     * @param chatId : 채팅 ID
     * @return : 새롭게 초대된 USER & 이미 초대된 USER IDs
     */
    @Transactional
    public Map<ChatListType, Set<Long>> updateChatParticipantsActive(Long chatId) {
        List<Participants> participants = participantsRepository.findByChatId(chatId)
                .orElse(new ArrayList<>());

        Map<ChatListType, Set<Long>> map = new HashMap<>();
        Set<Long> newChatParticipants = new HashSet<>();
        Set<Long> existChatParticipants = new HashSet<>();

        if (!participants.isEmpty()) {
            for (Participants participant : participants) {
                if (!participant.isActive()) {
                    newChatParticipants.add(participant.getUserId());
                } else {
                    existChatParticipants.add(participant.getUserId());
                }
                participant.changeStatus(true);
            }
        }
        map.put(ChatListType.NEW, newChatParticipants);
        map.put(ChatListType.EXIST, existChatParticipants);
        participantsRepository.saveAll(participants);

        return map;
    }


    /** 단톡 초대 메세지 생성.
     *
     *  단톡 초대 최초 메세지 생성 및 저장.
     * @param creatorId : 채팅 생성자 ID
     * @param chatId : 채팅 ID
     * @return
     */
    @Transactional
    public Message saveJoinMessage(Long creatorId, Long chatId) {
        List<UserDto> userDtos = userQueryRepository.searchParticipantsUserInfo(chatId);
        String inviteNotificationContent = createInviteNotificationContent(creatorId, userDtos);

        Message joinMessage = Message.builder()
                .chatId(chatId)
                .type(MessageType.NOTI)
                .userId(null)
                .content(inviteNotificationContent)
                .build();

        return messageRepository.save(joinMessage);
    }

    private String createInviteNotificationContent(Long creatorId, List<UserDto> userDtos) {
        StringBuilder content = new StringBuilder();

        UserDto creatorUserDto = userDtos.stream()
                .filter(userDto -> userDto.getId().equals(creatorId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 참여하지 않는 유저."));

        userDtos.remove(creatorUserDto);
        content.append(String.format("%s님이 ", creatorUserDto.getName()));

        IntStream.range(0, userDtos.size())
                .forEach(idx -> content.append(String.format("%s님,", userDtos.get(idx).getName())));

        content.delete(content.length() - 1, content.length());
        content.append("을 초대하였습니다.");

        return content.toString();
    }

    /** 최초 그룹 메세지 인지 확인.
     *
     *  이전에 보낸 메세지가 NOTI (초대 메세지)이면 최초 그룹 메세지.
     *   최초 메세지 발생 시점에 채팅 목록 구독중인 초대 유저들에게 GROUP Message 전달하기 위함.
     * @param chatId : 채팅 ID
     * @return
     */
    public boolean isInitGroupChatMessage(Long chatId) {
        List<Message> messages = messageQueryRepository.getMessagesByChatId(chatId, 1);

        if (!messages.isEmpty()) {
            return messages.get(0).getType().equals(MessageType.NOTI);
        }

        return false;
    }

    /** 저장된 메세지를 아직 읽지 않은 USER IDs
     *
     * @param saveMessage : 메세지.
     * @return : 저장된 메세지를 아직 읽지 않은 USER IDs
     */
    public Set<Long> getUnreadUserIds(Message saveMessage) {
        List<Participants> participants = participantsRepository.findByChatIdAndActive(saveMessage.getChatId(), true)
                .orElse(new ArrayList<>());
        Set<Long> unreadChatUserIds = new HashSet<>();

        for (Participants participant : participants) {
            if (participant.getLastAt() < saveMessage.getCreatedAt()) {
                unreadChatUserIds.add(participant.getUserId());
            }
        }

        return unreadChatUserIds;
    }

}
