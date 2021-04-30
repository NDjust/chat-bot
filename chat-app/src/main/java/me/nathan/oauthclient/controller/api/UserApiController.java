package me.nathan.oauthclient.controller.api;

import me.nathan.oauthclient.model.dto.response.api.CheckDto;
import me.nathan.oauthclient.model.dto.response.api.DefaultResponse;
import me.nathan.oauthclient.model.dto.response.api.UserDto;
import me.nathan.oauthclient.domain.User;
import me.nathan.oauthclient.service.JwtService;
import me.nathan.oauthclient.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserApiController {

    private UserDetailServiceImpl userDetailService;

    private JwtService jwtService;

    @Autowired
    public UserApiController(UserDetailServiceImpl userDetailService, JwtService jwtService) {
        this.userDetailService = userDetailService;
        this.jwtService = jwtService;
    }

    @GetMapping("/check")
    public ResponseEntity<DefaultResponse<CheckDto>> getUserInfo(@RequestHeader("Authorization") String authorization) {
        Long userId = jwtService.getUserIdFromToken(authorization);
        User user = userDetailService.getById(userId);
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .image(user.getProfileImage())
                .build();
        return ResponseEntity.ok(
                DefaultResponse.response(new CheckDto(userDto), 200, "성공")
        );
    }

    @GetMapping("/users")
    public DefaultResponse getUsers() {
        return userDetailService.getUsers();
    }
}
