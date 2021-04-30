package me.nathan.oauthclient.controller.api;

import me.nathan.oauthclient.model.dto.request.api.ChatIdsDto;
import me.nathan.oauthclient.model.dto.request.api.CreateChatDto;
import me.nathan.oauthclient.model.dto.response.api.DefaultResponse;
import me.nathan.oauthclient.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
public class ChatApiController {

    private ChatService chatService;

    private SearchChatService searchChatService;

    private JwtService jwtService;

    @Autowired
    public ChatApiController(ChatService chatService, SearchChatService searchChatService, JwtService jwtService) {
        this.chatService = chatService;
        this.searchChatService = searchChatService;
        this.jwtService = jwtService;
    }

    @GetMapping(value = "/chats")
    public DefaultResponse getChats(@RequestHeader("Authorization") String authorization) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        return searchChatService.getChats(userId);
    }
    @PostMapping(value = "/chats")
    public DefaultResponse create(@RequestHeader("Authorization") String authorization, @RequestBody CreateChatDto createChatDto) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        return chatService.create(createChatDto, userId);
    }

    @PutMapping(value = "/chats/{chatId}/unreadCnt")
    public DefaultResponse readChatInChatList(@RequestHeader("Authorization") String authorization, @PathVariable("chatId") Long chatId) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        return chatService.readChatInChatList(userId, chatId);
    }

    @PostMapping(value = "/chats/delete")
    public DefaultResponse outChat(@RequestHeader("Authorization") String authorization, @RequestBody ChatIdsDto chatIdsDto) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        return chatService.deleteChat(userId, chatIdsDto);
    }
}
