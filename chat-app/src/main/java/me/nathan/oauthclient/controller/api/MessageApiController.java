package me.nathan.oauthclient.controller.api;

import me.nathan.oauthclient.model.dto.response.api.DefaultResponse;
import me.nathan.oauthclient.service.JwtService;
import me.nathan.oauthclient.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
public class MessageApiController {

    private MessageService messageService;

    private JwtService jwtService;

    @Autowired
    public MessageApiController(MessageService messageService, JwtService jwtService) {
        this.messageService = messageService;
        this.jwtService = jwtService;
    }

    @GetMapping("/chats/{chatId}")
    public DefaultResponse getMessages(@RequestHeader("Authorization") String authorization,
                                       @RequestParam(value = "lastMessageId", required = false) Long lastMessageId,
                                       @RequestParam(value = "size", required = false) Long size,
                                       @PathVariable("chatId") Long chatId) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        return messageService.getMessages(userId, chatId, lastMessageId, size);
    }
}
