package me.nathan.oauthclient.model.dto.request.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChatIdsDto {

    private List<Long> chatIds;

    public ChatIdsDto(List<Long> chatIds) {
        this.chatIds = chatIds;
    }
}
