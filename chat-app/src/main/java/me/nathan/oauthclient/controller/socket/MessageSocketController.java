package me.nathan.oauthclient.controller.socket;

import lombok.extern.slf4j.Slf4j;
import me.nathan.oauthclient.model.dto.request.websocket.ChatMessageDto;
import me.nathan.oauthclient.model.dto.request.websocket.JoinChatDto;
import me.nathan.oauthclient.service.PublishingMessageService;
import me.nathan.oauthclient.service.ToxicFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class MessageSocketController {

    private final PublishingMessageService publishingMessageService;
    private final ToxicFilterService toxicFilterService;

    @Autowired
    public MessageSocketController(PublishingMessageService publishingMessageService,
                                   ToxicFilterService toxicFilterService) {
        this.publishingMessageService = publishingMessageService;
        this.toxicFilterService = toxicFilterService;
    }


    /** 개인 톡 메세지.
     *
     * @param message : 전달되는 메세지
     * @param messageHeaderAccessor : 전달되는 메세지의 헤더 값.
     */
    @MessageMapping("/chat/private/message")
    public void sendPrivateMessage(ChatMessageDto message, SimpMessageHeaderAccessor messageHeaderAccessor) {
        String sessionId = messageHeaderAccessor.getSessionId();
        toxicFilterService.isToxic(message.getContent());
        publishingMessageService.sendPrivateMessage(sessionId, message);
    }

    /** 그룹 톡 메세지
     *
     * @param message : 전달되는 메세지
     * @param messageHeaderAccessor : 전달되는 메세지의 헤더 값.
     */
    @MessageMapping("/chat/group/message")
    public void sendGroupMessage(ChatMessageDto message, SimpMessageHeaderAccessor messageHeaderAccessor) {
        String sessionId = messageHeaderAccessor.getSessionId();
        publishingMessageService.sendGroupMessage(sessionId, message);
    }

    /** 셀프 톡 메세지
     *
     * @param message : 전달되는 메세지
     * @param messageHeaderAccessor : 전달되는 메세지의 헤더 값.
     */
    @MessageMapping("/chat/self/message")
    public void sendSelfMessage(ChatMessageDto message, SimpMessageHeaderAccessor messageHeaderAccessor) {
        String sessionId = messageHeaderAccessor.getSessionId();
        publishingMessageService.sendSelfMessage(sessionId, message);
    }

    public void sendJoinMessage(String sessionId, JoinChatDto joinChatDto) {
        publishingMessageService.sendGroupInvitedMessage(sessionId, joinChatDto);
    }
}
