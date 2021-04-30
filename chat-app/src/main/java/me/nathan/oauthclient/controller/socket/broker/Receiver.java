package me.nathan.oauthclient.controller.socket.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.nathan.oauthclient.model.dto.request.websocket.ChatMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Service
public class Receiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    private final SimpMessagingTemplate template;

    public Receiver(final SimpMessagingTemplate template) {
        this.template = template;
    }

    @KafkaListener(id = "main-listener", topics = "kafka-chatting")
    public void receive(ChatMessageDto message) throws Exception {
        LOGGER.info("message='{}'", message);
        HashMap<String, String> msg = new HashMap<>();
        msg.put("timestamp", Long.toString(message.getTimeStamp()));
        msg.put("message", message.getContent());
        msg.put("author", String.valueOf(message.getChatId()));

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(msg);

        this.template.convertAndSend("/topic/public", json);
    }
}
