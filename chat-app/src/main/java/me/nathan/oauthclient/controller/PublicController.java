package me.nathan.oauthclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicController {
    private final Resource indexPage;

    public PublicController(@Value("classpath:/static/index.html") Resource indexPage) {
        this.indexPage = indexPage;
    }

    @GetMapping(value = {"/", "/loginPage", "/chat/**"})
    public ResponseEntity<Resource> getFirstPage() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity<>(indexPage, headers, HttpStatus.OK);
    }

}