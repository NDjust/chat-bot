package me.nathan.oauthclient.repository;

import me.nathan.oauthclient.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

    Optional<User> findByUsername(String username);

    @Query(value = "select * from user u where u.email = :email and u.provider_type = :providerType", nativeQuery = true)
    Optional<User> findByEmailAndProviderType(@Param("email") String email, @Param("providerType") String provideType);

}
