package com.caffzzzip.routine.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.routine.api.dto.RoutineRequest;
import com.caffzzzip.routine.application.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequiredArgsConstructor
@Tag(name = "Routine", description = "사용자 루틴 설정 API")
@RequestMapping("/api/users/me/routine")
public class RoutineController {

    private final RoutineService routineService;

    @Operation(
            summary = "초기 루틴 설정",
            description = """
                    최초 로그인 사용자의 수면 루틴과 카페인 관련 기본 정보를 저장합니다.
                    
                    이 API는 사용자가 처음 로그인한 뒤,
                    서비스의 개인 맞춤 카페인 분석을 사용하기 위해 반드시 한 번 설정해야 하는 초기 설정 API입니다.
                    
                    저장되는 정보는 다음과 같습니다.
                    
                    1. weekdayRoutineName
                    - 평일 루틴 이름입니다.
                    - 예: 수업, 출근, 평일
                    - 값을 보내지 않거나 빈 문자열로 보내면 기본값으로 "평소"가 저장됩니다.
                    
                    2. weekendRoutineName
                    - 주말 또는 쉬는 날 루틴 이름입니다.
                    - 예: 금공강, 쉬는 날, 주말
                    - 값을 보내지 않거나 빈 문자열로 보내면 기본값으로 "쉬는 날"이 저장됩니다.
                    
                    3. weekdayWakeTime
                    - 평일 기상 시간입니다.
                    - 예: 07:00
                    
                    4. weekdaySleepTime
                    - 평일 취침 시간입니다.
                    - 예: 23:00
                    
                    5. weekendWakeTime
                    - 주말 또는 쉬는 날 기상 시간입니다.
                    - 예: 09:00
                    
                    6. weekendSleepTime
                    - 주말 또는 쉬는 날 취침 시간입니다.
                    - 예: 01:00
                    
                    7. caffeineSensitivity
                    - 사용자의 카페인 민감도입니다.
                    - LOW: 낮음
                    - NORMAL: 보통
                    - HIGH: 높음
                    
                    8. intakeFrequency
                    - 사용자의 평소 카페인 섭취 빈도입니다.
                    - RARELY: 거의 안 마심
                    - SOMETIMES: 적게 마심
                    - OFTEN: 자주
                    - DAILY: 매우 자주
                    
                    이 루틴 정보는 다음 기능에서 사용됩니다.
                    
                    - 메뉴 상세 화면의 섭취 전 위험도 계산
                    - 취침 시간 기준 예상 잔존 카페인 계산
                    - 평일/주말 루틴 구분
                    - 마이페이지 루틴 조회 및 수정
                    - 오늘의 카페인 분석 리포트 계산
                    
                    주의할 점:
                    - routineType은 프론트에서 보내지 않습니다.
                    - 평일/주말 여부는 섭취 기록의 intakeAt 날짜를 기준으로 백엔드에서 자동 판단합니다.
                    - 같은 사용자가 이미 초기 루틴을 설정한 경우, 중복 설정이 제한될 수 있습니다.
                    
                    프론트 사용 흐름:
                    1. 카카오 로그인 후 isFirstLogin이 true인 경우 초기 설정 화면으로 이동합니다.
                    2. 사용자가 평일/주말 수면 시간, 카페인 민감도, 섭취 빈도를 입력합니다.
                    3. 이 API로 초기 루틴을 저장합니다.
                    4. 저장 완료 후 메인 화면 또는 기록 화면으로 이동합니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ApiResTemplate<?> createRoutine(
            Authentication authentication,
            @RequestBody RoutineRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        routineService.createRoutine(userId, request);

        return ApiResTemplate.successWithNoContent(SuccessCode.SAVE_SUCCESS);
    }
}