package me.nathan.oauthclient.repository;

import me.nathan.oauthclient.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByIdAndCreatorId(Long chatId, Long userId);
}
