package me.nathan.oauthclient.service;

import me.nathan.oauthclient.domain.Chat;
import me.nathan.oauthclient.domain.Message;
import me.nathan.oauthclient.domain.Participants;
import me.nathan.oauthclient.domain.type.MessageType;
import me.nathan.oauthclient.model.dto.response.api.DefaultResponse;
import me.nathan.oauthclient.model.dto.response.api.MessageDto;
import me.nathan.oauthclient.model.dto.response.api.MessagesDto;
import me.nathan.oauthclient.repository.ChatMessageUnreadCntRepository;
import me.nathan.oauthclient.repository.ChatRedisRepository;
import me.nathan.oauthclient.repository.ChatRepository;
import me.nathan.oauthclient.repository.ParticipantsRepository;
import me.nathan.oauthclient.repository.query.MessageQueryRepository;
import me.nathan.oauthclient.util.common.ResponseMessage;
import me.nathan.oauthclient.util.common.ResponseStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private MessageQueryRepository messageQueryRepository;

    private ParticipantsRepository participantsRepository;

    private ChatRedisRepository chatRedisRepository;

    private ChatMessageUnreadCntRepository chatMessageUnreadCntRepository;

    private ChatRepository chatRepository;

    private static final int MESSAGE_COUNT = 20;

    @Autowired
    public MessageService(MessageQueryRepository messageQueryRepository, ChatRedisRepository chatRedisRepository, ChatRepository chatRepository,
                          ParticipantsRepository participantsRepository, ChatMessageUnreadCntRepository chatMessageUnreadCntRepository) {
        this.messageQueryRepository = messageQueryRepository;
        this.participantsRepository = participantsRepository;
        this.chatRedisRepository = chatRedisRepository;
        this.chatRepository = chatRepository;
        this.chatMessageUnreadCntRepository = chatMessageUnreadCntRepository;
    }

    @Transactional
    public DefaultResponse getMessages(Long userId, Long chatId, Long lastMessageId, Long size) {
        List<MessageDto> messageDtos = findMessages(userId, chatId, lastMessageId, size);
        chatMessageUnreadCntRepository.addChatReadMessages(chatId, userId, messageDtos);

        return DefaultResponse.response(
                new MessagesDto(messageDtos), ResponseStatusCode.SUCCESS, ResponseMessage.MESSAGE_SEARCH_SUCCESS);
    }

    /**
     * 메세지 조회.
     *
     * - 채팅방을 나간 시점을 확인하여, 나가기 전에 메세지 목록을 반환하지 못하도록 처리.
     * - 해당 유저의 최초 채팅방 참여 시점과 메세지 생성 시점을 비교해 참여 시점 이후 메세지만 반환.
     * @param userId : 조회하는 유저 ID
     * @param chatId : 조회하는 채팅 ID
     * @return
     */
    private List<MessageDto> findMessages(Long userId, Long chatId, Long lastMessageId, Long size) {
        long messageCount = size == null ? MESSAGE_COUNT : size;
        List<Message> messages;

        if (lastMessageId == null) {
            messages = messageQueryRepository.getMessagesByChatIdAndUserId(chatId, userId, messageCount);
        } else {
            messages = messageQueryRepository.getMessagesByChatIdAndLastMessageId(chatId, userId, lastMessageId, messageCount);
        }

        messages.sort(new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return Long.compare(o1.getCreatedAt(), o2.getCreatedAt());
            }
        });

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NoSuchElementException("chat Id : " + chatId + ResponseMessage.CHAT_NOT_FOUND));

        return getMessageDtos(userId, messages, chat);
    }

    private List<MessageDto> getMessageDtos(Long userId, List<Message> messages, Chat chat) {
        switch (chat.getType()) {
            case SELF:
                return messages.stream()
                        .map(message -> getMessageDto(message, new HashSet<>(), 0L))
                        .collect(Collectors.toList());
            case PRIVATE:
                return getPrivateMessageDtos(userId, messages, chat);
            default:
                List<Participants> participants = participantsRepository.findByChatIdAndActive(chat.getId(), true)
                                                                            .orElse(new ArrayList<>());
                return calculateUnreadCnt(messages, participants);

        }
    }

    private List<MessageDto> getPrivateMessageDtos(Long userId, List<Message> messages, Chat chat) {
        List<Participants> participantsAll = participantsRepository.findByChatId(chat.getId())
                                                        .orElse(new ArrayList<>());
        Participants otherParticipants = participantsAll.stream()
                .filter(participant -> !participant.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        String chatId = String.valueOf(chat.getId());
        String userIdToString = String.valueOf(otherParticipants.getUserId());

        if (chatRedisRepository.hasChatUserId(chatId, userIdToString)) {
            return messages.stream()
                    .map(message -> getMessageDto(message, new HashSet<>(), 0L))
                    .collect(Collectors.toList());
        }

        return calculatePrivateChatUnreadCnt(messages, otherParticipants);
    }

    private List<MessageDto> calculatePrivateChatUnreadCnt(List<Message> messages, Participants participants) {
        List<MessageDto> messageDtos = new ArrayList<>();
        long unreadCnt = 0;
        Set<Long> userIds = new HashSet<>();

        for (Message message : messages) {
            if (participants.getLastOutAt() != null && participants.getLastAt() < message.getCreatedAt()
                                    && participants.getLastOutAt() > message.getCreatedAt()) {
                unreadCnt = 1L;
                userIds.add(participants.getUserId());
            } else if (participants.getLastAt() < message.getCreatedAt()) {
                unreadCnt = 1L;
                userIds.add(participants.getUserId());
            }

            MessageDto messageDto = getMessageDto(message, userIds, unreadCnt);
            messageDtos.add(messageDto);
        }
        return messageDtos;
    }

    /**
     * 각각의 메세지에 대해서 unreadCnt를 계산.
     *
     * -
     * @param messages : 마지막 20개 메세지
     * @param participants : 본 채팅에 참여한 참여자
     * @return
     */
    private List<MessageDto> calculateUnreadCnt(List<Message> messages, List<Participants> participants) {
        participants = removeSubscribeChatParticipants(participants);
        List<MessageDto> messageDtos = new ArrayList<>();

        for (Message message : messages) {
            long count = 0;
            Set<Long> userIds = new HashSet<>();
            for (Participants participant : participants) {

                if (message.getType().equals(MessageType.NOTI)) {
                    continue;
                }

                // 내가 보낸 메세지의 경우. skip
                if (!participant.getUserId().equals(message.getUserId())
                        && participant.getLastAt() < message.getCreatedAt()) {
                    count++;
                    userIds.add(participant.getUserId());
                }
            }
            MessageDto messageDto = getMessageDto(message, userIds, count);
            messageDtos.add(messageDto);
        }
        return messageDtos;
    }


    private List<Participants> removeSubscribeChatParticipants( List<Participants> participants) {
        List<Participants> notEnterParticipants = new ArrayList<>();
        for (Participants participant : participants) {
            String userId = String.valueOf(participant.getUserId());
            String chatId = String.valueOf(participant.getChatId());

            if (!chatRedisRepository.hasChatUserId(chatId, userId)) {
                notEnterParticipants.add(participant);
            }
        }
        return notEnterParticipants;
    }

    private MessageDto getMessageDto(Message message, Set<Long> userIds, long unreadCnt) {
        return MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .sentAt(message.getCreatedAt())
                .unreadCnt(unreadCnt)
                .type(message.getType())
                .userId(message.getUserId())
                .unreadCntUserIds(userIds)
                .build();
    }
}
