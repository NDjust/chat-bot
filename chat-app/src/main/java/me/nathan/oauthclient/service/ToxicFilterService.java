package me.nathan.oauthclient.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ToxicFilterService {

    private final RestTemplate restTemplate;

    public ToxicFilterService(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void isToxic(final String content) {

    }
}
