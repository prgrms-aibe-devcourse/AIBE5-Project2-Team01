package com.example.meetball.domain.profile.repository;

import com.example.meetball.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("select u from Profile u join u.account a where a.email = :email")
    Optional<Profile> findByEmail(String email);

    Optional<Profile> findByNickname(String nickname);

    Optional<Profile> findFirstByOrderByIdAsc();

    Optional<Profile> findFirstByAccount_IdAndDefaultProfileTrueOrderByIdAsc(Long accountId);

    Optional<Profile> findFirstByAccountSocialProviderAndAccountSocialIdentifierOrderByDefaultProfileDescIdAsc(String provider, String identifier);
}
