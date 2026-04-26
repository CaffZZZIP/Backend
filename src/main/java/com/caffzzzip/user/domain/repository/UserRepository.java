package com.caffzzzip.user.domain.repository;

import com.caffzzzip.user.domain.SocialType;
import com.caffzzzip.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}