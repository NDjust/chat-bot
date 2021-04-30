package me.nathan.oauthclient.repository;

import me.nathan.oauthclient.domain.ConnectedChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface ConnectedChatRepository extends JpaRepository<ConnectedChat, Long> {

    Optional<ConnectedChat> findByChatId(Long chatId);

    @Transactional
    @Modifying
    @Query(value = "update connected_chat c set c.last_at = :lastAt, c.last_message_id = :lastMessageId where c.chat_id = :chatId", nativeQuery = true)
    void updateLastAtAndLastMessageId(@Param("chatId") Long chatId, @Param("lastAt") long lastAt, @Param("lastMessageId") Long lastMessageId);
}
