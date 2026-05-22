package com.caffzzzip.menu.application;

import com.caffzzzip.common.error.ErrorCode;
import com.caffzzzip.common.exception.BusinessException;
import com.caffzzzip.intake.domain.IntakeLog;
import com.caffzzzip.intake.domain.repository.IntakeLogRepository;
import com.caffzzzip.menu.api.dto.BrandResponse;
import com.caffzzzip.menu.api.dto.MenuDetailResponse;
import com.caffzzzip.menu.api.dto.MenuResponse;
import com.caffzzzip.menu.domain.Menu;
import com.caffzzzip.menu.domain.RiskLevel;
import com.caffzzzip.menu.domain.repository.MenuRepository;
import com.caffzzzip.routine.domain.CaffeineSensitivity;
import com.caffzzzip.routine.domain.Routine;
import com.caffzzzip.routine.domain.RoutineType;
import com.caffzzzip.routine.domain.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private static final double CAFFEINE_HALF_LIFE_HOURS = 5.0;
    private static final int DAILY_RECOMMENDED_LIMIT = 400;

    private final MenuRepository menuRepository;
    private final IntakeLogRepository intakeLogRepository;
    private final RoutineRepository routineRepository;

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

    public MenuDetailResponse getMenuDetail(
            Long userId,
            Long menuId,
            LocalDateTime intakeAt,
            Integer quantity
    ) {
        Menu menu = findMenu(menuId);
        Routine routine = findRoutine(userId);

        LocalDateTime selectedIntakeAt = intakeAt != null
                ? intakeAt
                : LocalDateTime.now();

        int selectedQuantity = quantity != null
                ? quantity
                : 1;

        validateQuantity(selectedQuantity);

        int intakeCaffeine = menu.getCaffeineMg() * selectedQuantity;

        int todayTotalCaffeine = getTodayTotalCaffeine(
                userId,
                selectedIntakeAt.toLocalDate()
        );

        int expectedTotalCaffeine = todayTotalCaffeine + intakeCaffeine;

        RiskLevel riskLevel = calculateRiskLevel(
                expectedTotalCaffeine,
                routine.getCaffeineSensitivity()
        );

        RoutineType routineType = getRoutineType(selectedIntakeAt);

        LocalDateTime sleepDateTime = getSleepDateTime(
                selectedIntakeAt,
                routine,
                routineType
        );

        int expectedRemainingCaffeine = calculateExpectedRemainingCaffeine(
                userId,
                selectedIntakeAt.toLocalDate(),
                sleepDateTime,
                intakeCaffeine,
                selectedIntakeAt
        );

        String guideMessage = createGuideMessage(
                routine.getCaffeineSensitivity(),
                selectedIntakeAt,
                sleepDateTime,
                expectedRemainingCaffeine,
                expectedTotalCaffeine,
                riskLevel
        );

        return new MenuDetailResponse(
                menu.getId(),
                menu.getMenuName(),
                menu.getBrand(),
                menu.getCategory().getName(),
                menu.getCaffeineMg(),
                selectedQuantity,
                intakeCaffeine,
                todayTotalCaffeine,
                expectedTotalCaffeine,
                DAILY_RECOMMENDED_LIMIT,
                riskLevel,
                getRiskLabel(riskLevel),
                routine.getCaffeineSensitivity(),
                expectedRemainingCaffeine,
                guideMessage
        );
    }

    private Menu findMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "해당 메뉴를 찾을 수 없습니다."
                ));
    }

    private Routine findRoutine(Long userId) {
        return routineRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "사용자 루틴 설정을 찾을 수 없습니다."
                ));
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "수량은 1개 이상이어야 합니다."
            );
        }
    }

    private int getTodayTotalCaffeine(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return intakeLogRepository.findByUserIdAndIntakeAtBetween(userId, start, end)
                .stream()
                .mapToInt(IntakeLog::getTotalCaffeine)
                .sum();
    }

    private int calculateExpectedRemainingCaffeine(
            Long userId,
            LocalDate date,
            LocalDateTime sleepDateTime,
            int newIntakeCaffeine,
            LocalDateTime newIntakeAt
    ) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        int existingRemainingCaffeine = intakeLogRepository.findByUserIdAndIntakeAtBetween(userId, start, end)
                .stream()
                .mapToInt(intakeLog -> calculateRemainingCaffeine(
                        intakeLog.getTotalCaffeine(),
                        intakeLog.getIntakeAt(),
                        sleepDateTime
                ))
                .sum();

        int newRemainingCaffeine = calculateRemainingCaffeine(
                newIntakeCaffeine,
                newIntakeAt,
                sleepDateTime
        );

        return existingRemainingCaffeine + newRemainingCaffeine;
    }

    private RoutineType getRoutineType(LocalDateTime intakeAt) {
        DayOfWeek dayOfWeek = intakeAt.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return RoutineType.WEEKEND;
        }

        return RoutineType.WEEKDAY;
    }

    private LocalDateTime getSleepDateTime(
            LocalDateTime intakeAt,
            Routine routine,
            RoutineType routineType
    ) {
        LocalTime sleepTime = routineType == RoutineType.WEEKEND
                ? routine.getWeekendSleepTime()
                : routine.getWeekdaySleepTime();

        LocalDate sleepDate = intakeAt.toLocalDate();

        if (sleepTime.isBefore(intakeAt.toLocalTime()) || sleepTime.equals(intakeAt.toLocalTime())) {
            sleepDate = sleepDate.plusDays(1);
        }

        return LocalDateTime.of(sleepDate, sleepTime);
    }

    private int calculateRemainingCaffeine(
            int caffeineMg,
            LocalDateTime intakeAt,
            LocalDateTime sleepDateTime
    ) {
        long minutes = Duration.between(intakeAt, sleepDateTime).toMinutes();

        if (minutes <= 0) {
            return caffeineMg;
        }

        double hours = minutes / 60.0;
        double remaining = caffeineMg * Math.pow(0.5, hours / CAFFEINE_HALF_LIFE_HOURS);

        return (int) Math.round(remaining);
    }

    private RiskLevel calculateRiskLevel(
            int expectedTotalCaffeine,
            CaffeineSensitivity sensitivity
    ) {
        return switch (sensitivity) {
            case HIGH -> {
                if (expectedTotalCaffeine <= 150) {
                    yield RiskLevel.SAFE;
                } else if (expectedTotalCaffeine <= 250) {
                    yield RiskLevel.CAUTION;
                } else {
                    yield RiskLevel.DANGER;
                }
            }
            case NORMAL -> {
                if (expectedTotalCaffeine <= 300) {
                    yield RiskLevel.SAFE;
                } else if (expectedTotalCaffeine <= DAILY_RECOMMENDED_LIMIT) {
                    yield RiskLevel.CAUTION;
                } else {
                    yield RiskLevel.DANGER;
                }
            }
            case LOW -> {
                if (expectedTotalCaffeine <= DAILY_RECOMMENDED_LIMIT) {
                    yield RiskLevel.SAFE;
                } else if (expectedTotalCaffeine <= 500) {
                    yield RiskLevel.CAUTION;
                } else {
                    yield RiskLevel.DANGER;
                }
            }
        };
    }

    private String getRiskLabel(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case SAFE -> "안전";
            case CAUTION -> "주의";
            case DANGER -> "위험";
        };
    }

    private String createGuideMessage(
            CaffeineSensitivity sensitivity,
            LocalDateTime intakeAt,
            LocalDateTime sleepDateTime,
            int expectedRemainingCaffeine,
            int expectedTotalCaffeine,
            RiskLevel riskLevel
    ) {
        String baseMessage = String.format(
                "민감도 %s 기준으로, %s에 마시면 %s 무렵에는 약 %dmg 정도 남아있을 거예요.",
                getSensitivityLabel(sensitivity),
                formatTime(intakeAt.toLocalTime()),
                formatTime(sleepDateTime.toLocalTime()),
                expectedRemainingCaffeine
        );

        if (riskLevel == RiskLevel.DANGER || expectedTotalCaffeine > DAILY_RECOMMENDED_LIMIT) {
            return baseMessage + " 오늘 예상 총 카페인 섭취량이 " + expectedTotalCaffeine
                    + "mg으로 하루 권장량을 넘을 수 있어요. 잠깐 카페인을 쉬는 게 좋을 것 같아요!";
        }

        if (riskLevel == RiskLevel.CAUTION) {
            return baseMessage + " 오늘 예상 총 카페인 섭취량이 " + expectedTotalCaffeine
                    + "mg이에요. 늦은 시간 추가 섭취는 조금 주의해주세요.";
        }

        return baseMessage;
    }

    private String getSensitivityLabel(CaffeineSensitivity sensitivity) {
        return switch (sensitivity) {
            case LOW -> "낮음";
            case NORMAL -> "보통";
            case HIGH -> "높음";
        };
    }

    private String formatTime(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();

        String period = hour < 12 ? "오전" : "오후";
        int displayHour = hour % 12;

        if (displayHour == 0) {
            displayHour = 12;
        }

        if (minute == 0) {
            return String.format("%s %d시", period, displayHour);
        }

        return String.format("%s %d시 %02d분", period, displayHour, minute);
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