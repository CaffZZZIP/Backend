package com.caffzzzip.favorite.domain.repository;

import com.caffzzzip.favorite.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndMenuId(Long userId, Long menuId);

    Optional<Favorite> findByUserIdAndMenuId(Long userId, Long menuId);

    List<Favorite> findByUserId(Long userId);
}