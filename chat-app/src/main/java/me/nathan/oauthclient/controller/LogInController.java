package me.nathan.oauthclient.controller;

import me.nathan.oauthclient.model.dto.response.api.OwnerInfoDto;
import me.nathan.oauthclient.service.CustomOAuthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LogInController {

    private CustomOAuthUserService userService;


    @Autowired
    public LogInController(CustomOAuthUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard(ModelAndView mv, @AuthenticationPrincipal OAuth2User user) {
        String username = user.getName();
        OwnerInfoDto userInfo = userService.getUserInfo(username);

        mv.addObject("userInfo", userInfo);
        mv.addObject("userJson", user.getAttributes());
        mv.setViewName("dashboard");
        return mv;
    }

}
