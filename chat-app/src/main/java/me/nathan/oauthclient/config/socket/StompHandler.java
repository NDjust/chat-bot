package me.nathan.oauthclient.config.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.nathan.oauthclient.controller.socket.MessageSocketController;
import me.nathan.oauthclient.util.common.SubscribeTopic;
import me.nathan.oauthclient.model.dto.request.websocket.JoinChatDto;
import me.nathan.oauthclient.repository.SocketEventRepository;
import me.nathan.oauthclient.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    @Autowired
    private MessageSendService messageSendService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private SocketEventRepository socketEventRepository;
    @Autowired
    private ChatService chatService;
    @Autowired
    private MessageSocketController messageSocketController;


    // web-socket 을 통해 들어온 요청이 처리 되기전 실행.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT == accessor.getCommand()) { // websocket 연결요청
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            String sessionId = accessor.getSessionId();

            validConnectToken(jwtToken);
            saveConnectSessionId(jwtToken, sessionId);

            log.info("CONNECTED Token : {}, Session Id : {}", jwtToken, sessionId);
        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) { // 채팅룸 구독요청
            subscribe(message, accessor);
        } else if (StompCommand.DISCONNECT == accessor.getCommand()) { // Websocket 연결 종료
            disconnect(message);
        } else if (StompCommand.SEND == accessor.getCommand()) {
            String destination = accessor.getDestination();
            String sessionId = accessor.getSessionId();
            // CHAT_JOIN SEND Intercept 해서, 우선 처리.
            // CHAT_JOIN SEND 먼저 처리해야 초대 메세지 후 첫 메세지 발송이 됨.
            if (destination.equals(SubscribeTopic.CHAT_JOIN_TOPIC)) {
                try {
                    byte[] payload = (byte[]) message.getPayload();
                    ObjectMapper objectMapper = new ObjectMapper();
                    JoinChatDto joinChatDto = objectMapper.readValue(payload, JoinChatDto.class);
                    messageSocketController.sendJoinMessage(sessionId, joinChatDto);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return message;
    }

    // 연결 session에 대한 userId 값 저장.
    private void saveConnectSessionId(String jwtToken, String sessionId) {
        Long userId = jwtService.getUserIdFromToken(jwtToken);
        socketEventRepository.setEnterUserIdBySessionId(userId, sessionId);
    }

    public void validConnectToken(String jwtToken) {
        // Header의 jwt token 검증
        if (!jwtService.validateToken(jwtToken)) {
            throw new IllegalArgumentException("Not valid Token");
        }
    }

    /**
     * 연결이 종료된 클라이언트 session Id로 채팅방 id를 얻는다.
     *
     * @param message : web-socket message
     */
    public void disconnect(Message<?> message) {
        String sessionId = (String) message.getHeaders().get("simpSessionId");
        String chatId = socketEventRepository.getEnterChatId(sessionId);
        String userId = socketEventRepository.getEnterUserId(sessionId);

        // remove enter chat log
        if (chatId != null) {
            socketEventRepository.disconnectChat(sessionId);
            chatService.outChat(Long.parseLong(userId), Long.parseLong(chatId));
        }

        socketEventRepository.removeSubscribeChatListUser(sessionId);
        log.info("DISCONNECTED {}, {}", sessionId, chatId);
    }


    /**
     * 메세지 페이지 구독 : [subscribe] /sub/chat/rooms/{chatId}
     * 채팅 목록 구독 : [subscribe] /sub/chat/list/{userId}
     * 실시간 메세지 읽음 구독 : [subscribe] /sub/chat/unreadCnt/{chatId}/{userId}
     *
     * 구독 Topic에 따라서, 이벤트 처리.
     * @param message
     * @param accessor
     */
    public void subscribe(Message<?> message, StompHeaderAccessor accessor) {
        // 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다.(나중에 특정 세션이 어떤 채팅방에 들어가 있는지 알기 위함)
        String sessionId = (String) message.getHeaders().get("simpSessionId");
        String destination = accessor.getDestination();
        String destinationFormat = destination.replaceAll("[0-9]", "");

        // 구독 Topic 구분.
        switch (destinationFormat) {
            case SubscribeTopic.CHAT_TOPIC + "/": {
                socketEventRepository.addSubscribeChatListUser(sessionId);
                break;
            }
            case SubscribeTopic.MESSAGE_TOPIC + "/": {
                String chatId = getDestinationId(destination);
                String userId = socketEventRepository.getEnterUserId(sessionId);
                socketEventRepository.subscribeToChat(sessionId, chatId);

                // 새롭게 누가 채팅에 입장 했으므로, unreadCnt 갱신.
//                messageSendService.sendUserChatUnreadCnt(chatId, userId);
                String lastConnectedChatTime = socketEventRepository.getLastEnteredChatTime(sessionId);
                messageSendService.sendAndUpdateUnreadCnt(userId, chatId, lastConnectedChatTime);
                chatService.enterChat(Long.parseLong(userId), Long.parseLong(chatId));
                break;
            }
            case SubscribeTopic.UNREAD_COUNT_TOPIC + "/": {
                break;
            }
        }

        log.info("SUBSCRIBED SESSION ID : {}, DESTINATION  : {}", sessionId, destination);
    }

    private String getDestinationId(String destination) {
        int lastIndex = destination.lastIndexOf('/');

        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return null;
    }

}
