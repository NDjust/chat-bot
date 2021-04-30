package me.nathan.oauthclient.model.dto.response.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.nathan.oauthclient.model.dao.MemberInfo;

import java.util.List;

@Data
@NoArgsConstructor
public class MembersDto {

    private List<MemberInfo> members;

    public MembersDto(List<MemberInfo> members) {
        this.members = members;
    }
}
