package me.nathan.oauthclient.repository;

import me.nathan.oauthclient.model.dao.UserChatMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserChatsRepository {

    List<UserChatMapper> getUserChats(Long userId);
}
