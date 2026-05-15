package com.caffzzzip.menu.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.menu.api.dto.BrandResponse;
import com.caffzzzip.menu.api.dto.MenuDetailResponse;
import com.caffzzzip.menu.api.dto.MenuResponse;
import com.caffzzzip.menu.application.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
@Tag(name = "Menu", description = "카페인 메뉴 API")
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "브랜드 목록 조회")
    @GetMapping("/brands")
    public ApiResTemplate<List<BrandResponse>> getBrands() {
        List<BrandResponse> response = menuService.getBrands();
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }

    @Operation(summary = "카테고리별 메뉴 목록 조회")
    @GetMapping("/category/{categoryId}")
    public ApiResTemplate<List<MenuResponse>> getMenusByCategory(
            @PathVariable Long categoryId
    ) {
        List<MenuResponse> response = menuService.getMenusByCategory(categoryId);
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }

    @Operation(summary = "메뉴 목록 조회")
    @GetMapping
    public ApiResTemplate<List<MenuResponse>> getMenus(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long categoryId
    ) {
        List<MenuResponse> response = menuService.getMenus(brand, categoryId);
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }

    @Operation(summary = "메뉴 검색")
    @GetMapping("/search")
    public ApiResTemplate<List<MenuResponse>> searchMenus(
            @RequestParam String keyword,
            @RequestParam(required = false) String brand
    ) {
        List<MenuResponse> response = menuService.searchMenus(keyword, brand);
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }

    @Operation(
            summary = "메뉴 상세 조회",
            description = "메뉴 정보와 사용자 루틴, 오늘 섭취량을 기반으로 위험도와 예상 잔존 카페인을 계산합니다."
    )
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{menuId}")
    public ApiResTemplate<MenuDetailResponse> getMenuDetail(
            Authentication authentication,
            @PathVariable Long menuId,
            @RequestParam(required = false) LocalDateTime intakeAt,
            @RequestParam(required = false) Integer quantity
    ) {
        Long userId = Long.valueOf(authentication.getName());

        MenuDetailResponse response = menuService.getMenuDetail(
                userId,
                menuId,
                intakeAt,
                quantity
        );

        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }
}