package com.caffzzzip.menu.application;

import com.caffzzzip.common.error.ErrorCode;
import com.caffzzzip.common.exception.BusinessException;
import com.caffzzzip.menu.api.dto.BrandResponse;
import com.caffzzzip.menu.api.dto.MenuDetailResponse;
import com.caffzzzip.menu.api.dto.MenuResponse;
import com.caffzzzip.menu.domain.Menu;
import com.caffzzzip.menu.domain.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;

    public List<BrandResponse> getBrands() {
        return menuRepository.findByIsActiveTrue().stream()
                .map(Menu::getBrand)
                .distinct()
                .sorted()
                .map(BrandResponse::new)
                .toList();
    }

    public List<MenuResponse> getMenusByCategory(Long categoryId) {
        return menuRepository.findByCategoryIdAndIsActiveTrue(categoryId).stream()
                .sorted(Comparator.comparing(Menu::getMenuName))
                .map(this::toMenuResponse)
                .toList();
    }

    public List<MenuResponse> getMenus(String brand, Long categoryId) {
        List<Menu> menus;

        if (brand != null && !brand.isBlank() && categoryId != null) {
            menus = menuRepository.findByBrandAndCategoryIdAndIsActiveTrue(brand, categoryId);
        } else if (brand != null && !brand.isBlank()) {
            menus = menuRepository.findByBrandAndIsActiveTrue(brand);
        } else if (categoryId != null) {
            menus = menuRepository.findByCategoryIdAndIsActiveTrue(categoryId);
        } else {
            menus = menuRepository.findByIsActiveTrue();
        }

        return menus.stream()
                .sorted(Comparator.comparing(Menu::getMenuName))
                .map(this::toMenuResponse)
                .toList();
    }

    public List<MenuResponse> searchMenus(String keyword, String brand) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "검색어를 입력해주세요."
            );
        }

        List<Menu> menus;

        if (brand != null && !brand.isBlank()) {
            menus = menuRepository.findByBrandAndMenuNameContainingAndIsActiveTrue(brand, keyword);
        } else {
            menus = menuRepository.findByMenuNameContainingAndIsActiveTrue(keyword);
        }

        return menus.stream()
                .sorted(Comparator.comparing(Menu::getMenuName))
                .map(this::toMenuResponse)
                .toList();
    }

    public MenuDetailResponse getMenuDetail(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "해당 메뉴를 찾을 수 없습니다."
                ));

        return new MenuDetailResponse(
                menu.getId(),
                menu.getMenuName(),
                menu.getBrand(),
                menu.getCategory().getName(),
                menu.getCaffeineMg()
        );
    }

    private MenuResponse toMenuResponse(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getMenuName(),
                menu.getBrand(),
                menu.getCategory().getName(),
                menu.getCaffeineMg()
        );
    }
}