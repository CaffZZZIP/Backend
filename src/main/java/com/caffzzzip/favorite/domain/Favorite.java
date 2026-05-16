package com.caffzzzip.favorite.domain;

import com.caffzzzip.menu.domain.Menu;
import com.caffzzzip.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_favorite_user_menu",
                        columnNames = {"user_id", "menu_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 즐겨찾기한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 즐겨찾기한 메뉴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Builder
    public Favorite(User user, Menu menu) {
        this.user = user;
        this.menu = menu;
    }

    public boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }
}