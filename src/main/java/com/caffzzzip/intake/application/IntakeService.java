package com.caffzzzip.intake.application;

import com.caffzzzip.common.error.ErrorCode;
import com.caffzzzip.common.exception.BusinessException;
import com.caffzzzip.intake.api.dto.IntakeCreateRequest;
import com.caffzzzip.intake.api.dto.IntakeResponse;
import com.caffzzzip.intake.domain.IntakeLog;
import com.caffzzzip.intake.domain.repository.IntakeLogRepository;
import com.caffzzzip.menu.domain.Menu;
import com.caffzzzip.menu.domain.repository.MenuRepository;
import com.caffzzzip.user.domain.User;
import com.caffzzzip.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntakeService {

    private final IntakeLogRepository intakeLogRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public IntakeResponse createIntake(Long userId, IntakeCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.MEMBER_NOT_FOUND,
                        "해당 사용자를 찾을 수 없습니다."
                ));

        Menu menu = menuRepository.findById(request.menuId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "해당 메뉴를 찾을 수 없습니다."
                ));

        int totalCaffeine = menu.getCaffeineMg() * request.quantity();

        IntakeLog intakeLog = IntakeLog.builder()
                .user(user)
                .menu(menu)
                .routineType(request.routineType())
                .intakeAt(request.intakeAt())
                .quantity(request.quantity())
                .totalCaffeine(totalCaffeine)
                .build();

        IntakeLog savedIntakeLog = intakeLogRepository.save(intakeLog);

        return new IntakeResponse(
                savedIntakeLog.getId(),
                menu.getId(),
                menu.getMenuName(),
                menu.getBrand(),
                savedIntakeLog.getRoutineType(),
                savedIntakeLog.getIntakeAt(),
                savedIntakeLog.getQuantity(),
                savedIntakeLog.getTotalCaffeine()
        );
    }
}