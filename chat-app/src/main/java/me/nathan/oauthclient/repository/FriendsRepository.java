package me.nathan.oauthclient.repository;

import me.nathan.oauthclient.domain.Friends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, Long> {
    void deleteByUserIdAndFriendId(Long userId, Long friendId);

    Optional<Friends> findByUserIdAndFriendId(Long userId, Long friendId);
}
