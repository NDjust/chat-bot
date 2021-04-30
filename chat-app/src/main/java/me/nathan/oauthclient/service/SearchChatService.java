package me.nathan.oauthclient.service;

import me.nathan.oauthclient.model.dao.MemberMapper;
import me.nathan.oauthclient.model.dao.UserChatMapper;
import me.nathan.oauthclient.model.dto.response.api.ChatMessageUnreadCntDto;
import me.nathan.oauthclient.model.dto.response.api.ChatsDto;
import me.nathan.oauthclient.model.dto.response.api.DefaultResponse;
import me.nathan.oauthclient.model.dto.response.api.UserChatDto;
import me.nathan.oauthclient.repository.ChatRedisRepository;
import me.nathan.oauthclient.repository.UserChatsRepository;
import me.nathan.oauthclient.repository.query.MessageQueryRepository;
import me.nathan.oauthclient.util.common.ResponseMessage;
import me.nathan.oauthclient.util.common.ResponseStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SearchChatService {

    private UserChatsRepository userChatsRepository;

    private ChatRedisRepository chatRedisRepository;

    private MessageQueryRepository messageQueryRepository;

    @Autowired
    public SearchChatService(UserChatsRepository userChatsRepository, ChatRedisRepository chatRedisRepository,
                             MessageQueryRepository messageQueryRepository) {
        this.userChatsRepository = userChatsRepository;
        this.chatRedisRepository = chatRedisRepository;
        this.messageQueryRepository = messageQueryRepository;
    }

    @Transactional
    public DefaultResponse getChats(Long userId) {
        List<UserChatMapper> userChats = userChatsRepository.getUserChats(userId);
        List<ChatMessageUnreadCntDto> unreadCnt = messageQueryRepository.getChatMessageUnreadCnt(userId);

        addUnreadCnt(userId, userChats, unreadCnt);
        addChatUnreadCntInRedis(userId, userChats);

        List<UserChatDto> userChatDtos = convertDto(userChats, userId);

        return DefaultResponse.response(new ChatsDto(userChatDtos),
                ResponseStatusCode.SUCCESS, ResponseMessage.CHAT_SEARCH_SUCCESS);
    }

    private void addUnreadCnt(Long userId, List<UserChatMapper> userChats, List<ChatMessageUnreadCntDto> unreadCnt) {
        for (int i = 0; i < userChats.size(); i++) {
            UserChatMapper userChatMapper = userChats.get(i);
            ChatMessageUnreadCntDto chatMessageUnreadCntDto = unreadCnt.get(i);
            userChatMapper.setUnreadCnt(chatMessageUnreadCntDto.getUnreadCnt());
        }
    }

    /** 채팅 목록 조회 시, Redis에 각 조회한 채팅 목록 저장.
     *
     * 각 채팅 UnreadCnt 및 lastMessage 업데이트를 위해 저장.
     * @param userId : 채팅 목록 조회한 유저 ID
     * @param userChats : 조회한 유저 채팅 목록 정보.
     */
    private void addChatUnreadCntInRedis(Long userId, List<UserChatMapper> userChats) {
        for (UserChatMapper userChat : userChats) {
            chatRedisRepository.addUserChatUnreadCnt(userId, userChat.getId(), userChat.getUnreadCnt());
        }
    }


    private List<UserChatDto> convertDto(List<UserChatMapper> userChats, Long userId) {
        List<UserChatDto> userChatDtos = new ArrayList<>();

        // 나간 이후 메세지는 제외.
        for (int i = 0; i < userChats.size(); i++) {
            UserChatMapper chatMapper = userChats.get(i);
            MemberMapper userParticipantsInfo = chatMapper.getMember(userId);

            if (userParticipantsInfo.getLastOutAt() != null &&
                    userParticipantsInfo.getLastOutAt() > chatMapper.getLastAt()) {
                chatMapper.setLastMessage("");
                chatMapper.setLastAt(userParticipantsInfo.getLastEnterAt());
            }
            userChatDtos.add(chatMapper.toUserChatDto());
        }

        userChatDtos.sort(new Comparator<UserChatDto>() {
            @Override
            public int compare(UserChatDto o1, UserChatDto o2) {
                return Long.compare(o2.getLastAt(), o1.getLastAt());
            }
        });

        return userChatDtos;
    }
}
