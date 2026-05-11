package com.caffzzzip.menu.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메뉴가 속한 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 메뉴명 예: 아메리카노, 몬스터 에너지, 녹차
    @Column(nullable = false, length = 100)
    private String menuName;

    // 브랜드명 예: 스타벅스, 메가커피, 몬스터
    @Column(nullable = false, length = 100)
    private String brand;

    // 1잔 기준 카페인 함량
    @Column(nullable = false)
    private Integer caffeineMg;

    // 메뉴 활성화 여부
    @Column(nullable = false)
    private Boolean isActive;
}