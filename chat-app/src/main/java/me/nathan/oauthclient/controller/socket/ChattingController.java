package me.nathan.oauthclient.controller.socket;

import me.nathan.oauthclient.controller.socket.broker.ChattingHistoryDAO;
import me.nathan.oauthclient.controller.socket.broker.Receiver;
import me.nathan.oauthclient.controller.socket.broker.Sender;
import me.nathan.oauthclient.model.dto.request.websocket.ChatMessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class ChattingController {

    private static final String BOOT_TOPIC = "kafka-chatting";

    private final Sender sender;

    private final Receiver receiver;

    private final ChattingHistoryDAO chattingHistoryDAO;

    @Autowired
    public ChattingController(final Sender sender, final Receiver receiver, final ChattingHistoryDAO chattingHistoryDAO) {
        this.sender = sender;
        this.receiver = receiver;
        this.chattingHistoryDAO = chattingHistoryDAO;
    }


    //// "url/app/message"로 들어오는 메시지를 "/topic/public"을 구독하고있는 사람들에게 송신
    @MessageMapping("/message")//@MessageMapping works for WebSocket protocol communication. This defines the URL mapping.
    @SendTo("/topic/public")//websocket subscribe topic& direct send
    public void sendMessage(ChatMessageDto message) {
        message.setTimeStamp(System.currentTimeMillis());
        chattingHistoryDAO.save(message);
        sender.send(BOOT_TOPIC, message);
    }

    @RequestMapping("/history")
    public List<ChatMessageDto> getChattingHistory() {
        System.out.println("history!");
        return chattingHistoryDAO.get();
    }

    @MessageMapping("/file")
    @SendTo("/topic/chatting")
    public ChatMessageDto sendFile(ChatMessageDto message) {
        return new ChatMessageDto(message.getType(), message.getChatId(), message.getContent());
    }

}
