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
import com.caffzzzip.routine.domain.UserDailyRoutineMode;
import com.caffzzzip.routine.domain.repository.RoutineRepository;
import com.caffzzzip.routine.domain.repository.UserDailyRoutineModeRepository;
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

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final int DAILY_RECOMMENDED_LIMIT = 400;

    private final MenuRepository menuRepository;
    private final IntakeLogRepository intakeLogRepository;
    private final RoutineRepository routineRepository;
    private final UserDailyRoutineModeRepository userDailyRoutineModeRepository;

    public List<BrandResponse> getBrands() {
        return menuRepository.findByIsActiveTrue().stream()
                .map(Menu::getBrand)
                .distinct()
                .sorted()
                .map(brand -> new BrandResponse(
                        brand,
                        getBrandLogoUrl(brand)
                ))
                .toList();
    }

    private String getBrandLogoUrl(String brand) {
        return switch (brand) {
            case "스타벅스" -> "/images/brands/starbucks.png";
            case "이디야" -> "/images/brands/ediya.png";
            case "할리스" -> "/images/brands/hollys.png";
            case "메가커피" -> "/images/brands/mega-coffee.png";
            case "빽다방" -> "/images/brands/paikdabang.png";
            case "더벤티" -> "/images/brands/theventi.png";
            case "에너지드링크" -> "/images/brands/energy-drink.png";
            case "커피빈" -> "/images/brands/coffeebean.png";
            case "폴바셋" -> "/images/brands/paulbassett.png";
            case "투썸플레이스" -> "/images/brands/twosomeplace.png";
            case "탐앤탐스" -> "/images/brands/tomntoms.png";
            default -> "/images/brands/default.png";
        };
    }

    public List<MenuResponse> getMenusByCategory(Long categoryId) {
        List<Menu> menus = menuRepository.findByCategoryIdAndIsActiveTrue(categoryId);
        return sortAndConvertToMenuResponses(menus);
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

        return sortAndConvertToMenuResponses(menus);
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

        return sortAndConvertToMenuResponses(menus);
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
                : LocalDateTime.now(KOREA_ZONE_ID);

        int selectedQuantity = quantity != null ? quantity : 1;
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

        RoutineType routineType = getSelectedRoutineType(
                userId,
                selectedIntakeAt.toLocalDate()
        );

        LocalDateTime sleepDateTime = getSleepDateTime(
                selectedIntakeAt,
                routine,
                routineType
        );

        double halfLifeHours = getHalfLifeHours(routine.getCaffeineSensitivity());

        int expectedRemainingCaffeine = calculateExpectedRemainingCaffeine(
                userId,
                selectedIntakeAt.toLocalDate(),
                sleepDateTime,
                intakeCaffeine,
                selectedIntakeAt,
                halfLifeHours
        );

        String guideMessage = createGuideMessage(
                routine.getCaffeineSensitivity(),
                selectedIntakeAt,
                sleepDateTime,
                expectedRemainingCaffeine,
                expectedTotalCaffeine,
                riskLevel,
                halfLifeHours
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

    private RoutineType getSelectedRoutineType(Long userId, LocalDate date) {
        return userDailyRoutineModeRepository.findByUserIdAndTargetDate(userId, date)
                .map(UserDailyRoutineMode::getRoutineType)
                .orElseGet(() -> getRoutineType(date.atStartOfDay()));
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
            LocalDateTime newIntakeAt,
            double halfLifeHours
    ) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        int existingRemainingCaffeine = intakeLogRepository.findByUserIdAndIntakeAtBetween(userId, start, end)
                .stream()
                .mapToInt(intakeLog -> calculateRemainingCaffeine(
                        intakeLog.getTotalCaffeine(),
                        intakeLog.getIntakeAt(),
                        sleepDateTime,
                        halfLifeHours
                ))
                .sum();

        int newRemainingCaffeine = calculateRemainingCaffeine(
                newIntakeCaffeine,
                newIntakeAt,
                sleepDateTime,
                halfLifeHours
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
            LocalDateTime sleepDateTime,
            double halfLifeHours
    ) {
        long minutes = Duration.between(intakeAt, sleepDateTime).toMinutes();

        if (minutes <= 0) {
            return caffeineMg;
        }

        double hours = minutes / 60.0;
        double remaining = caffeineMg * Math.pow(0.5, hours / halfLifeHours);

        return (int) Math.round(remaining);
    }

    private double getHalfLifeHours(CaffeineSensitivity sensitivity) {
        return switch (sensitivity) {
            case LOW -> 4.0;
            case NORMAL -> 5.0;
            case HIGH -> 6.0;
        };
    }

    private RiskLevel calculateRiskLevel(
            int expectedTotalCaffeine,
            CaffeineSensitivity sensitivity
    ) {
        return switch (sensitivity) {
            case HIGH -> {
                if (expectedTotalCaffeine <= 150) yield RiskLevel.SAFE;
                else if (expectedTotalCaffeine <= 250) yield RiskLevel.CAUTION;
                else yield RiskLevel.DANGER;
            }
            case NORMAL -> {
                if (expectedTotalCaffeine <= 300) yield RiskLevel.SAFE;
                else if (expectedTotalCaffeine <= DAILY_RECOMMENDED_LIMIT) yield RiskLevel.CAUTION;
                else yield RiskLevel.DANGER;
            }
            case LOW -> {
                if (expectedTotalCaffeine <= DAILY_RECOMMENDED_LIMIT) yield RiskLevel.SAFE;
                else if (expectedTotalCaffeine <= 500) yield RiskLevel.CAUTION;
                else yield RiskLevel.DANGER;
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
            RiskLevel riskLevel,
            double halfLifeHours
    ) {
        String baseMessage = String.format(
                "민감도 %s 기준, 이 음료의 카페인 반감기는 약 %.0f시간이에요. %s에 마시면 %s 무렵에는 약 %dmg 정도 남아있어요.",
                getSensitivityLabel(sensitivity),
                halfLifeHours,
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

    private List<MenuResponse> sortAndConvertToMenuResponses(List<Menu> menus) {
        return menus.stream()
                .sorted(
                        Comparator.comparing((Menu menu) -> menu.getCategory().getId())
                                .thenComparing(Menu::getMenuName)
                )
                .map(this::toMenuResponse)
                .toList();
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