package me.nathan.oauthclient.repository;

import me.nathan.oauthclient.domain.Participants;
import me.nathan.oauthclient.domain.type.StatusType;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantsRepository extends JpaRepository<Participants, Long> {

    void deleteByChatIdAndUserId(Long chatId, Long userId);

    Optional<List<Participants>> findByChatId(Long chatId);

    Optional<Participants> findByChatIdAndUserId(Long chatId, Long userId);

    Optional<List<Participants>> findByChatIdAndActive(Long id, boolean active);

    Optional<List<Participants>> findByUserId(Long userId);

    Optional<List<Participants>> findByUserIdAndActive(Long userId, boolean b);

    @Query("UPDATE participants p SET p.lastAt = :lastAt WHERE p.userId = :userId AND p.chatId = :chatId")
    void updateLastAt(@Param("userId") Long userId, @Param("chatId") Long chatId, @Param("lastAt") long lastAt);
}
