package me.nathan.oauthclient.repository;

import me.nathan.oauthclient.domain.ConnectedChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnteredChatRepository extends JpaRepository<ConnectedChat, Long> {
    Optional<ConnectedChat> findByChatId(Long id);

    void deleteByChatId(Long chatId);
}
