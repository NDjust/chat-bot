package me.nathan.oauthclient.controller.api;

import me.nathan.oauthclient.model.dto.response.api.DefaultResponse;
import me.nathan.oauthclient.model.dto.response.api.MembersDto;
import me.nathan.oauthclient.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ParticipantsApiController {

    private UserDetailServiceImpl userDetailService;

    @Autowired
    public ParticipantsApiController(UserDetailServiceImpl userDetailService) {
        this.userDetailService = userDetailService;
    }

    @GetMapping("/chats/{chatId}/members")
    public DefaultResponse<MembersDto> getChatParticipants(@RequestHeader("Authorization") String authorization,
                                                           @PathVariable("chatId") Long chatId) {
        return userDetailService.getChatParticipants(chatId);
    }
}
