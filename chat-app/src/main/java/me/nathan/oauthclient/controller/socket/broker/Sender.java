package me.nathan.oauthclient.controller.socket.broker;

import me.nathan.oauthclient.model.dto.request.websocket.ChatMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class Sender {

    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);

    private final KafkaTemplate<String, ChatMessageDto> kafkaTemplate;

    public Sender(final KafkaTemplate<String, ChatMessageDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, ChatMessageDto data) {
        LOG.info("sending data='{}' to topic='{}'", data, topic);
        kafkaTemplate.send(topic, data);// send to react clients via websocket(STOMP)
    }
}
