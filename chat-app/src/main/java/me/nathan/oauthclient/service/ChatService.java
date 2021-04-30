package me.nathan.oauthclient.service;

import me.nathan.oauthclient.domain.*;
import me.nathan.oauthclient.domain.type.ChatType;
import me.nathan.oauthclient.domain.type.MessageType;
import me.nathan.oauthclient.model.dto.request.api.ChatIdsDto;
import me.nathan.oauthclient.model.dto.request.api.CreateChatDto;
import me.nathan.oauthclient.model.dto.response.api.DefaultResponse;
import me.nathan.oauthclient.model.dto.response.api.InitChatDto;
import me.nathan.oauthclient.model.dto.response.api.UserDto;
import me.nathan.oauthclient.repository.*;
import me.nathan.oauthclient.model.dto.response.api.WaitingParticipantChatDto;
import me.nathan.oauthclient.repository.query.ParticipantsQueryRepository;
import me.nathan.oauthclient.util.common.ResponseMessage;
import me.nathan.oauthclient.util.common.ResponseStatusCode;
import me.nathan.oauthclient.model.dto.response.websocket.InitCreateChatDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private ChatRepository chatRepository;

    private ParticipantsRepository participantsRepository;

    private UserRepository userRepository;

    private ParticipantsQueryRepository participantsQueryRepository;

    private MessageSendService messageSendService;

    private MessageRepository messageRepository;

    private ConnectedChatRepository connectedChatRepository;


    @Autowired
    public ChatService(ChatRepository chatRepository, ParticipantsRepository participantsRepository,
                       UserRepository userRepository, ParticipantsQueryRepository participantsQueryRepository,
                       MessageSendService messageSendService, MessageRepository messageRepository,
                       ConnectedChatRepository connectedChatRepository) {
        this.chatRepository = chatRepository;
        this.participantsRepository = participantsRepository;
        this.userRepository = userRepository;
        this.participantsQueryRepository = participantsQueryRepository;
        this.messageSendService = messageSendService;
        this.messageRepository = messageRepository;
        this.connectedChatRepository = connectedChatRepository;
    }

    /**
     * GROUP & PRIVATE & SELF
     * 각각 채팅 타입에 따라 비즈니스 로직 반영.
     *
     * GROUP은 매번 첫 생성자로 인해 매번 새롭게 생성.
     * PRIVATE & SELF는 상대 & 본인의 생성 및 나간 시점을 계산해서 비즈니스 로직 반영.
     * @param createChatDto : 채팅 생성 Request DTO
     * @param userId : 생성 요청 userId
     * @return
     */
    @Transactional
    public DefaultResponse create(CreateChatDto createChatDto, Long userId) {
        // save Chat
        Chat chat = saveChatAndConnectedChat(createChatDto, userId);
        // save Participants
        List<Long> friendIds = createChatDto.getFriendIds();
        InitCreateChatDto chatDto = convertChatDto(friendIds, userId, chat);
        return DefaultResponse.response(new InitChatDto(chatDto), ResponseStatusCode.SUCCESS, ResponseMessage.CHAT_CREATE_SUCCESS);
    }

    /** 그룹 채팅은 매번 새롭게 채팅방 생성, SELF & PRIVATE 이전에 생성한 채팅방 활성화.
     *
     * @param createChatDto : 생성한 Chat Dto
     * @param userId : 생성한 userId
     * @return Chat : Chat
     */
    private Chat saveChatAndConnectedChat(CreateChatDto createChatDto, Long userId) {
        // PRIVATE & SELF Chat
        if (!isGroupChat(createChatDto)) {
            return getPreviousOrCreateChat(createChatDto, userId);
        } else {
            Chat chat = saveChat(createChatDto, userId, ChatType.GROUP);
            chatRepository.save(chat);
            saveConnectedChat(chat);
            saveParticipants(createChatDto.getFriendIds(), userId, chat);
            return chat;
        }
    }

    private boolean isGroupChat(CreateChatDto createChatDto) {
        return createChatDto.getFriendIds().size() >= 2;
    }

    private InitCreateChatDto convertChatDto(List<Long> participantIds, Long userId, Chat chat) {
        participantIds.add(userId);
        List<UserDto> users = findUsers(participantIds);
        return InitCreateChatDto.builder()
                .id(chat.getId())
                .image(chat.getImage())
                .name(chat.getName())
                .lastMessage(null)
                .lastAt(chat.getLastAt())
                .type(chat.getType())
                .unreadCnt(0L)
                .members(users)
                .build();
    }

    private List<UserDto> findUsers(List<Long> participantIds) {
        return participantIds.stream()
                .map(id -> {
                    User user = userRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException(id + " 로 가입한 유저가 없습니다."));
                    return UserDto.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .image(user.getProfileImage())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** 채팅에 초대된 유저들에 대해 Participants 등록 후 비활성화.
     *
     * 채팅방이 실제 개설 시 활성화.
     * @param friendIds : 채팅에 초대된 친구들 ID.
     * @param creatorId : 채팅 생성한 생성자 ID
     * @param chat
     */
    @Transactional
    private void saveParticipants(List<Long> friendIds, Long creatorId, Chat chat) {
        Set<Participants> participants = new HashSet<>();

        // 초대한 참여자(친구)들은 대기 상태로 추가.
        if (!friendIds.isEmpty()) {
            participants = friendIds.stream()
                    .map(id -> getParticipants(chat, id, false))
                    .collect(Collectors.toSet());
        }

        Participants creatorParticipants = participantsRepository.findByChatIdAndUserId(chat.getId(), creatorId)
                                                .orElse(null);

        if (creatorParticipants == null) {
            // 생성자는 초대 상태로 생성.
            Participants creator = getParticipants(chat, creatorId, true);
            participants.add(creator);
        }

        participantsRepository.saveAll(participants);
    }

    private Participants getParticipants(Chat chat, Long id, boolean active) {
        return Participants.builder()
                .userId(id)
                .chatId(chat.getId())
                .chatType(chat.getType())
                .active(active)
                .build();
    }

    private Participants getParticipants(Long chatId, Long userId, ChatType type) {
        return Participants.builder()
                .userId(userId)
                .chatId(chatId)
                .chatType(type)
                .build();
    }

    /**
     * SELF & PRIVATE Chatting Logic
     *
     * SELF
     * - 기존에 채팅이 있으면 반환.
     *
     * PRIVATE
     * - PRIVATE 채팅의 경우 상대방만 채팅방을 생성해놓을 수 있음.
     *      - 위의 경우 해당 채팅방을 조회하고 참여 상태를 바꿈. (active : false -> true)
     * - 혼자만 나간 상태일 수 있음.
     *      - 기존 상대의 채팅방을 조회해, 해당 채팅방에 다시 INVITED 상태로 Participants 생성.
     * @param createChatDto  : 생성하는 채칭 request dto
     * @param userId : 생성하는 유저
     * @return
     */
    private Chat getPreviousOrCreateChat(CreateChatDto createChatDto, Long userId) {
        // SELF CHAT
        if (createChatDto.getFriendIds().isEmpty() || createChatDto.getFriendIds() == null) {
            Chat previousSelfChat = participantsQueryRepository.getPreviousSelfChat(userId);

            if (previousSelfChat != null) {
                return previousSelfChat;
            }

            // 이전에 생성된 채팅이 없으면 채팅방 생성 및 참여자 등록.
            Chat chat = saveChat(createChatDto, userId, ChatType.SELF);
            saveConnectedChat(chat);
            saveParticipants(createChatDto.getFriendIds(), userId, chat);
            return chat;
            // PRIVATE CHAT
        } else {
            Long friendId = createChatDto.getFriendIds().get(0);
            WaitingParticipantChatDto previousParticipantChat = participantsQueryRepository.getPreviousParticipantChat(friendId, userId);

            // 이미 생성된 채팅이(상대방이 이미 생성해놓은 채팅방) 있으면 기존 채팅방 반환.
            if (previousParticipantChat != null) {
                Long chatId = previousParticipantChat.getChatId();
                Long previousUserId = previousParticipantChat.getUserId();
                Participants previousParticipant = participantsRepository.findByChatIdAndUserId(chatId, previousUserId)
                                                    .orElse(null);

                // 해당 채팅방에서 이전에 나간 상태이며 다시 participant 생성.
                if (previousParticipant == null) {
                    previousParticipant = getParticipants(chatId, previousUserId, ChatType.PRIVATE);
                }

                // 참여 상태 및 마지막 참여 시점 갱신. (status & lsat_at)
                previousParticipant.changeStatus(true);
                previousParticipant.setLastAt(new Date().getTime());

                participantsRepository.save(previousParticipant);
                return chatRepository.findById(chatId)
                                        .orElseThrow(() -> new IllegalArgumentException("Not Found"));
            }

            // 기존에 생성된 채팅이 없으면, 채팅방 생성 및 참여자 등록.
            Chat chat = saveChat(createChatDto, userId, ChatType.PRIVATE);
            saveConnectedChat(chat);
            saveParticipants(createChatDto.getFriendIds(), userId, chat);
            return chat;
        }
    }

    private Chat saveChat(CreateChatDto createChatDto, Long userId, ChatType chatType) {
        Chat chat = Chat.builder()
                .image(createChatDto.getImage())
                .name(createChatDto.getName())
                .creatorId(userId)
                .type(chatType)
                .build();
        return chatRepository.save(chat);
    }

    private void saveConnectedChat(Chat chat) {
        ConnectedChat connectedChat = ConnectedChat.builder()
                .lastMessageId(null)
                .chatId(chat.getId())
                .build();
        connectedChatRepository.save(connectedChat);
    }

    /** 채팅방 삭제
     *
     *  채팅방 다중 삭제 진행하고, 채팅방 참여 여부를 비활성화로 바꾸고, 채팅방 나간 시점 갱신.
     *  -> 나중에 다시 초대 시, 채팅방 나간 이후 메세지만 조회할 수 있도록.
     * @param userId : 채팅방 삭제 userId
     * @param chatIdsDto : 삭제할 채팅방 IDs
     * @return
     */
    @Transactional
    public DefaultResponse deleteChat(Long userId, ChatIdsDto chatIdsDto) {
        List<Long> chatIds = chatIdsDto.getChatIds();
        List<Long> deleteSuccessIds = getSuccessDeleteChatIds(userId, chatIds);

        if (!deleteSuccessIds.isEmpty()) {
            List<Chat> chats = chatRepository.findAllById(chatIds);
            sendDeleteChatUserEvent(userId, chats);
            return DefaultResponse.response(new ChatIdsDto(deleteSuccessIds), ResponseStatusCode.SUCCESS, ResponseMessage.CHAT_OUT_SUCCESS);
        }
        return DefaultResponse.response(ResponseStatusCode.FORBIDDEN, ResponseMessage.CHAT_NOT_FOUND);
    }

    private void sendDeleteChatUserEvent(Long outUserId, List<Chat> chats) {
        User user = userRepository.findById(outUserId)
                .orElseThrow(() -> new IllegalArgumentException(outUserId + " 로 가입된 유저가 없습니다."));
        String outMessageContent = String.format("%s님이 나갔습니다.", user.getName());


        for (Chat chat : chats) {
            Long chatId = chat.getId();

            if (chat.getType().equals(ChatType.GROUP)) {
                Message outMessage = Message.builder()
                        .type(MessageType.NOTI)
                        .chatId(chatId)
                        .content(outMessageContent)
                        .build();
                messageRepository.save(outMessage);
                messageSendService.outUserChat(outUserId, chatId);
                messageSendService.sendGroupChatOutUser(chatId, outUserId, outMessage);
            }
        }
    }

    private List<Long> getSuccessDeleteChatIds(Long userId, List<Long> chatIds) {
        List<Participants> participants = new ArrayList<>();

        for (Long chatId : chatIds) {
            participantsRepository.findByChatIdAndUserId(chatId, userId).ifPresent(participants::add);

        }

        // change status & return success chatIds
        if (!participants.isEmpty()) {
            List<Long> successChatIds = new ArrayList<>();
            List<Participants> participantsList = participants.stream()
                    .peek(participant -> {
                        participant.changeStatus(false);
                        participant.outChat(new Date().getTime());
                        successChatIds.add(participant.getChatId());
                    })
                    .collect(Collectors.toList());
            participantsRepository.saveAll(participantsList);
            return successChatIds;
        }

        return new ArrayList<>();
    }

    /** 유저가 채팅한 입장 이벤트 실행.
     *
     *  유저가 입장시 participants lastAt 갱신.
     * @param userId : 입장한 유저 ID
     * @param chatId : 입장한 채팅 ID
     * @return
     */
    @Transactional
    public void enterChat(Long userId, Long chatId) {
        participantsRepository.updateLastAt(userId, chatId, new Date().getTime());
    }


    /** 유저가 채팅한 나간 이벤트 실행.
     *
     * 유저가 나갈 participants lastAt 갱신.
     * @param userId : 채팅방 나간 유저 ID
     * @param chatId : 나간 채팅방 ID
     * @return
     */
    @Transactional
    public void outChat(Long userId, Long chatId) {
        participantsRepository.updateLastAt(userId, chatId, new Date().getTime());
    }

    /** 특정 채팅방 읽음 처리.
     *
     *
     *  특정 채팅방 읽음 API 요청시, 해당 채팅방에 들어가 있는 유저 UnreadCnt 갱신.(Web Socket)
     *  -> 참여자의 마지막 참여 시점을 갱신해서, readChat & unreadCnt 로직 반영.
     * @param userId : 유저 ID
     * @param chatId : 읽음 처리한 채팅 ID
     * @return
     */
    @Transactional
    public DefaultResponse readChatInChatList(Long userId, Long chatId) {
        try {
            messageSendService.readUserChat(userId, chatId);
            participantsRepository.updateLastAt(userId, chatId, new Date().getTime());
            return DefaultResponse.response(ResponseStatusCode.SUCCESS, ResponseMessage.CHAT_READ_SUCCESS);
        } catch (IllegalArgumentException argumentException) {
            return DefaultResponse.response(ResponseStatusCode.BAD_REQUEST, argumentException.getMessage());
        }
    }
}
