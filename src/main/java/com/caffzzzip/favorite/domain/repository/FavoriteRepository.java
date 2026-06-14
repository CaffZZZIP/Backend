package com.caffzzzip.favorite.domain.repository;

import com.caffzzzip.favorite.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndMenuId(Long userId, Long menuId);

    Optional<Favorite> findByUserIdAndMenuId(Long userId, Long menuId);

    List<Favorite> findByUserId(Long userId);

    //회원탈퇴
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}