package me.nathan.oauthclient.controller.api;

import me.nathan.oauthclient.model.dto.response.api.DefaultResponse;
import me.nathan.oauthclient.model.dto.request.api.NewFriendDto;
import me.nathan.oauthclient.service.FriendsService;
import me.nathan.oauthclient.service.JwtService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FriendsApiController {

    private FriendsService friendsService;

    private JwtService jwtService;


    public FriendsApiController(FriendsService friendsService, JwtService jwtService) {
        this.friendsService = friendsService;
        this.jwtService = jwtService;
    }

    @GetMapping(value = "/friends")
    public DefaultResponse getFriends(@RequestHeader("Authorization") String authorization) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        return friendsService.getFriends(userId);
    }

    @PostMapping(value = "/friends")
    public DefaultResponse addFriend(@RequestHeader("Authorization") String authorization,
                                     @RequestBody NewFriendDto newFriendDto) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        return friendsService.addFriend(userId, newFriendDto);
    }

    @DeleteMapping(value = "/friends/{friendId}")
    public DefaultResponse deleteFriend(@RequestHeader("Authorization") String authorization,
                                        @PathVariable("friendId") Long friendId) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        return friendsService.deleteFriend(userId, friendId);
    }
}
