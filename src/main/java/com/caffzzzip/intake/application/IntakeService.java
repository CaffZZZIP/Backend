package com.caffzzzip.intake.application;

import com.caffzzzip.common.error.ErrorCode;
import com.caffzzzip.common.exception.BusinessException;
import com.caffzzzip.intake.api.dto.IntakeCreateRequest;
import com.caffzzzip.intake.api.dto.IntakeResponse;
import com.caffzzzip.intake.api.dto.IntakeUpdateRequest;
import com.caffzzzip.intake.domain.IntakeLog;
import com.caffzzzip.intake.domain.repository.IntakeLogRepository;
import com.caffzzzip.menu.domain.Menu;
import com.caffzzzip.menu.domain.repository.MenuRepository;
import com.caffzzzip.routine.domain.RoutineType;
import com.caffzzzip.user.domain.User;
import com.caffzzzip.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntakeService {

    private final IntakeLogRepository intakeLogRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    @Transactional
    public IntakeResponse createIntake(Long userId, IntakeCreateRequest request) {
        validateQuantity(request.quantity());

        User user = findUser(userId);
        Menu menu = findMenu(request.menuId());

        LocalDateTime intakeAt = request.intakeAt() != null
                ? request.intakeAt()
                : LocalDateTime.now(KOREA_ZONE_ID);

        RoutineType routineType = getRoutineType(intakeAt);
        int totalCaffeine = menu.getCaffeineMg() * request.quantity();

        IntakeLog intakeLog = IntakeLog.builder()
                .user(user)
                .menu(menu)
                .routineType(routineType)
                .intakeAt(intakeAt)
                .quantity(request.quantity())
                .totalCaffeine(totalCaffeine)
                .build();

        IntakeLog savedIntakeLog = intakeLogRepository.save(intakeLog);

        return toResponse(savedIntakeLog);
    }

    public List<IntakeResponse> getTodayIntakes(Long userId) {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return intakeLogRepository.findByUserIdAndIntakeAtBetween(userId, start, end).stream()
                .sorted(Comparator.comparing(IntakeLog::getIntakeAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public IntakeResponse updateIntake(Long userId, Long intakeId, IntakeUpdateRequest request) {
        validateQuantity(request.quantity());

        IntakeLog intakeLog = intakeLogRepository.findById(intakeId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "해당 섭취 기록을 찾을 수 없습니다."
                ));

        validateOwner(intakeLog, userId);

        LocalDateTime intakeAt = request.intakeAt() != null
                ? request.intakeAt()
                : intakeLog.getIntakeAt();

        RoutineType routineType = getRoutineType(intakeAt);
        int totalCaffeine = intakeLog.getMenu().getCaffeineMg() * request.quantity();

        intakeLog.update(
                intakeAt,
                request.quantity(),
                routineType,
                totalCaffeine
        );

        return toResponse(intakeLog);
    }

    @Transactional
    public void deleteIntake(Long userId, Long intakeId) {
        IntakeLog intakeLog = intakeLogRepository.findById(intakeId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "해당 섭취 기록을 찾을 수 없습니다."
                ));

        validateOwner(intakeLog, userId);

        intakeLogRepository.delete(intakeLog);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.MEMBER_NOT_FOUND,
                        "해당 사용자를 찾을 수 없습니다."
                ));
    }

    private Menu findMenu(Long menuId) {
        if (menuId == null) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "메뉴 ID는 필수입니다."
            );
        }

        return menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "해당 메뉴를 찾을 수 없습니다."
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

    private void validateOwner(IntakeLog intakeLog, Long userId) {
        if (!intakeLog.isOwner(userId)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN_EXCEPTION,
                    "해당 섭취 기록에 접근할 권한이 없습니다."
            );
        }
    }

    private RoutineType getRoutineType(LocalDateTime intakeAt) {
        DayOfWeek dayOfWeek = intakeAt.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return RoutineType.WEEKEND;
        }

        return RoutineType.WEEKDAY;
    }

    private IntakeResponse toResponse(IntakeLog intakeLog) {
        Menu menu = intakeLog.getMenu();

        return new IntakeResponse(
                intakeLog.getId(),
                menu.getId(),
                menu.getMenuName(),
                menu.getBrand(),
                intakeLog.getRoutineType(),
                intakeLog.getIntakeAt(),
                intakeLog.getQuantity(),
                intakeLog.getTotalCaffeine()
        );
    }
}