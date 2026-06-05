package com.caffzzzip.intake.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.intake.api.dto.IntakeCreateRequest;
import com.caffzzzip.intake.api.dto.IntakeResponse;
import com.caffzzzip.intake.api.dto.IntakeUpdateRequest;
import com.caffzzzip.intake.application.IntakeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/intake")
@Tag(name = "Intake", description = "카페인 섭취 기록 API")
public class IntakeController {

    private final IntakeService intakeService;

    @Operation(
            summary = "카페인 섭취 기록 저장",
            description = """
                    사용자가 실제로 섭취한 메뉴 정보를 저장합니다.
                    
                    사용자는 메뉴 상세 화면에서
                    섭취 예정 시간과 수량을 입력한 뒤,
                    "기록에 추가하기" 버튼을 눌러 이 API를 호출합니다.
                    
                    이 API는 단순 저장 API이며,
                    위험도 계산이나 예상 잔존 카페인 계산은
                    GET /api/menus/{menuId}에서 미리 수행합니다.
                    
                    저장 시 처리되는 정보는 다음과 같습니다.
                    
                    1. menuId
                    - 사용자가 섭취한 메뉴 ID입니다.
                    
                    2. intakeAt
                    - 실제 섭취 시간입니다.
                    - 값을 보내지 않으면 현재 시간 기준으로 저장됩니다.
                    
                    3. quantity
                    - 섭취 수량입니다.
                    
                    4. totalCaffeine
                    - 메뉴 카페인 함량 × quantity 기준으로 자동 계산됩니다.
                    
                    5. routineType
                    - intakeAt 기준으로 WEEKDAY / WEEKEND를 자동 계산합니다.
                    - 사용자가 직접 입력하지 않습니다.
                    
                    프론트 사용 흐름은 다음과 같습니다.
                    
                    1. 사용자가 메뉴 상세 화면에서 시간과 수량을 입력합니다.
                    2. GET /api/menus/{menuId}로 예상 위험도와 안내 문구를 확인합니다.
                    3. 사용자가 기록에 추가하기 버튼을 누르면 이 API를 호출합니다.
                    4. 저장 완료 후 오늘 섭취 기록과 결과 리포트에 반영됩니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ApiResTemplate<IntakeResponse> createIntake(
            Authentication authentication,
            @RequestBody IntakeCreateRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        IntakeResponse response = intakeService.createIntake(userId, request);

        return ApiResTemplate.successResponse(SuccessCode.SAVE_SUCCESS, response);
    }

    @Operation(
            summary = "오늘 카페인 섭취 기록 조회",
            description = """
                    로그인한 사용자의 오늘 카페인 섭취 기록 목록을 조회합니다.
                    
                    조회 기준:
                    - 오늘 00:00부터 다음 날 00:00 전까지의 intakeAt 기준
                    
                    반환 정보:
                    - 메뉴명
                    - 브랜드명
                    - 섭취 시간
                    - 섭취 수량
                    - 총 카페인량
                    
                    마이페이지 및 오늘 섭취 기록 리스트 화면에서 사용됩니다.
                    
                    반환 순서는 최신 섭취 기록 기준 내림차순입니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @GetMapping("/today")
    public ApiResTemplate<List<IntakeResponse>> getTodayIntakes(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        List<IntakeResponse> response = intakeService.getTodayIntakes(userId);

        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }

    @Operation(
            summary = "카페인 섭취 기록 수정",
            description = """
                    사용자가 저장한 카페인 섭취 기록을 수정합니다.
                    
                    수정 가능 정보:
                    - 섭취 시간(intakeAt)
                    - 섭취 수량(quantity)
                    
                    수정 시 다음 정보들이 자동으로 다시 계산됩니다.
                    
                    1. totalCaffeine
                    - 메뉴 카페인 함량 × 수정된 수량
                    
                    2. routineType
                    - 메인 화면에서 오늘 선택한 루틴 모드가 있으면 해당 값을 우선 적용합니다.
                    - 선택된 값이 없으면 intakeAt 날짜 기준으로 WEEKDAY / WEEKEND를 자동 판단합니다.
                    - 사용자가 직접 입력하지 않습니다.
                    
                    수정 후 오늘 카페인 통계와 결과 리포트에도 반영됩니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{intakeId}")
    public ApiResTemplate<IntakeResponse> updateIntake(
            Authentication authentication,

            @Parameter(description = "수정할 섭취 기록 ID", example = "1")
            @PathVariable Long intakeId,

            @RequestBody IntakeUpdateRequest request
    ) {
        Long userId = Long.valueOf(authentication.getName());

        IntakeResponse response = intakeService.updateIntake(userId, intakeId, request);

        return ApiResTemplate.successResponse(SuccessCode.UPDATE_SUCCESS, response);
    }

    @Operation(
            summary = "카페인 섭취 기록 삭제",
            description = """
                    사용자가 저장한 카페인 섭취 기록을 삭제합니다.
                    
                    삭제 후:
                    - 오늘 총 카페인량
                    - 예상 잔존 카페인량
                    - 위험도
                    
                    등의 결과가 다시 계산됩니다.
                    
                    본인이 저장한 섭취 기록만 삭제할 수 있습니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{intakeId}")
    public ApiResTemplate<?> deleteIntake(
            Authentication authentication,

            @Parameter(description = "삭제할 섭취 기록 ID", example = "1")
            @PathVariable Long intakeId
    ) {
        Long userId = Long.valueOf(authentication.getName());

        intakeService.deleteIntake(userId, intakeId);

        return ApiResTemplate.successWithNoContent(SuccessCode.INTAKE_DELETE_SUCCESS);
    }
}