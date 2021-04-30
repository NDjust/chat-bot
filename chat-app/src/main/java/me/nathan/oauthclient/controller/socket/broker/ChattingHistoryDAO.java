package me.nathan.oauthclient.controller.socket.broker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.nathan.oauthclient.model.dto.request.websocket.ChatMessageDto;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ChattingHistoryDAO {

    // A simple cache for temporarily storing chat data
    private final Cache<UUID, ChatMessageDto> chatHistoryCache =
            CacheBuilder
            .newBuilder().maximumSize(20).expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public void save(ChatMessageDto chatObj) {
        this.chatHistoryCache.put(UUID.randomUUID(), chatObj);
    }

    public List<ChatMessageDto> get() {
        return chatHistoryCache.asMap().values().stream()
                .sorted(Comparator.comparing(ChatMessageDto::getTimeStamp))
                .collect(Collectors.toList());
    }
}
