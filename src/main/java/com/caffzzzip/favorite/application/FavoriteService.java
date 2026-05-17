package com.caffzzzip.favorite.application;

import com.caffzzzip.common.error.ErrorCode;
import com.caffzzzip.common.exception.BusinessException;
import com.caffzzzip.favorite.api.dto.FavoriteResponse;
import com.caffzzzip.favorite.domain.Favorite;
import com.caffzzzip.favorite.domain.repository.FavoriteRepository;
import com.caffzzzip.menu.domain.Menu;
import com.caffzzzip.menu.domain.repository.MenuRepository;
import com.caffzzzip.user.domain.User;
import com.caffzzzip.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public FavoriteResponse addFavorite(Long userId, Long menuId) {
        User user = findUser(userId);
        Menu menu = findMenu(menuId);

        if (favoriteRepository.existsByUserIdAndMenuId(userId, menuId)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "이미 즐겨찾기한 메뉴입니다."
            );
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .menu(menu)
                .build();

        Favorite savedFavorite = favoriteRepository.save(favorite);

        return toResponse(savedFavorite);
    }

    @Transactional
    public void deleteFavorite(Long userId, Long menuId) {
        Favorite favorite = favoriteRepository.findByUserIdAndMenuId(userId, menuId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "해당 즐겨찾기를 찾을 수 없습니다."
                ));

        favoriteRepository.delete(favorite);
    }

    public List<FavoriteResponse> getFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(favorite -> favorite.getMenu().getMenuName()))
                .map(this::toResponse)
                .toList();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.MEMBER_NOT_FOUND,
                        "해당 사용자를 찾을 수 없습니다."
                ));
    }

    private Menu findMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "해당 메뉴를 찾을 수 없습니다."
                ));
    }

    private FavoriteResponse toResponse(Favorite favorite) {
        Menu menu = favorite.getMenu();

        return new FavoriteResponse(
                favorite.getId(),
                menu.getId(),
                menu.getMenuName(),
                menu.getBrand(),
                menu.getCategory().getName(),
                menu.getCaffeineMg()
        );
    }
}