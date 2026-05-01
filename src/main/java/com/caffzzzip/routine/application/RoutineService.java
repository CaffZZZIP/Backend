package com.caffzzzip.routine.application;

import com.caffzzzip.common.error.ErrorCode;
import com.caffzzzip.common.exception.BusinessException;
import com.caffzzzip.routine.api.dto.RoutineRequest;
import com.caffzzzip.routine.domain.Routine;
import com.caffzzzip.routine.domain.repository.RoutineRepository;
import com.caffzzzip.user.domain.User;
import com.caffzzzip.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createRoutine(Long userId, RoutineRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.MEMBER_NOT_FOUND,
                        "해당 사용자를 찾을 수 없습니다."
                ));

        if (routineRepository.existsByUserId(userId)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "이미 초기 루틴 설정이 완료된 사용자입니다."
            );
        }

        Routine routine = Routine.builder()
                .user(user)
                .weekdayWakeTime(request.weekdayWakeTime())
                .weekdaySleepTime(request.weekdaySleepTime())
                .weekendWakeTime(request.weekendWakeTime())
                .weekendSleepTime(request.weekendSleepTime())
                .caffeineSensitivity(request.caffeineSensitivity())
                .intakeFrequency(request.intakeFrequency())
                .build();

        routineRepository.save(routine);

        user.completeInitialSetting();
    }
}