package com.caffzzzip.menu.domain.repository;

import com.caffzzzip.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByCategoryIdAndIsActiveTrue(Long categoryId);

    List<Menu> findByMenuNameContainingAndIsActiveTrue(String keyword);

    List<Menu> findByBrandAndIsActiveTrue(String brand);

    List<Menu> findByBrandAndCategoryIdAndIsActiveTrue(String brand, Long categoryId);

    List<Menu> findByBrandAndMenuNameContainingAndIsActiveTrue(String brand, String keyword);

    List<Menu> findByIsActiveTrue();
}