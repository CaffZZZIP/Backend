package com.caffzzzip.favorite.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.favorite.api.dto.FavoriteResponse;
import com.caffzzzip.favorite.application.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
@Tag(name = "Favorite", description = "메뉴 즐겨찾기 API")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "메뉴 즐겨찾기 추가")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{menuId}")
    public ApiResTemplate<FavoriteResponse> addFavorite(
            Authentication authentication,
            @PathVariable Long menuId
    ) {
        Long userId = Long.valueOf(authentication.getName());

        FavoriteResponse response = favoriteService.addFavorite(userId, menuId);

        return ApiResTemplate.successResponse(SuccessCode.SAVE_SUCCESS, response);
    }

    @Operation(summary = "메뉴 즐겨찾기 해제")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{menuId}")
    public ApiResTemplate<?> deleteFavorite(
            Authentication authentication,
            @PathVariable Long menuId
    ) {
        Long userId = Long.valueOf(authentication.getName());

        favoriteService.deleteFavorite(userId, menuId);

        return ApiResTemplate.successWithNoContent(SuccessCode.FAVORITE_DELETE_SUCCESS);
    }

    @Operation(summary = "내 즐겨찾기 메뉴 목록 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ApiResTemplate<List<FavoriteResponse>> getFavorites(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        List<FavoriteResponse> response = favoriteService.getFavorites(userId);

        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }
}