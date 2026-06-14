package com.caffzzzip.my_page.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.my_page.api.dto.MyPageResponse;
import com.caffzzzip.my_page.api.dto.MyPageRoutineResponse;
import com.caffzzzip.my_page.application.MyPageService;
import com.caffzzzip.routine.api.dto.RoutineRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 API")
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(
            summary = "사용자 루틴 조회",
            description = """
                    로그인한 사용자의 루틴 설정 정보를 조회합니다.
                    
                    조회 정보:
                    - 평일/주말 루틴 이름
                    - 평일/주말 기상 시간
                    - 평일/주말 취침 시간
                    - 카페인 민감도
                    - 카페인 섭취 빈도
                    
                    메뉴 상세 화면의 카페인 위험도 계산과
                    예상 잔존 카페인 계산 시 사용되는 기준 데이터입니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @GetMapping("/api/users/me/routine")
    public ResponseEntity<MyPageRoutineResponse> getMyRoutine(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        return ResponseEntity.ok(
                myPageService.getMyRoutineInfo(userId)
        );
    }

    @Operation(
            summary = "주간 카페인 통계 조회",
            description = """
                    로그인한 사용자의 최근 7일 카페인 섭취 통계를 조회합니다.
                    
                    조회 정보:
                    - 요일별 총 카페인 섭취량
                    - 위험도
                    - 사용자 루틴 정보
                    
                    마이페이지의 주간 통계 그래프 데이터로 사용됩니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @GetMapping("/api/report/stats")
    public ResponseEntity<MyPageResponse> getWeeklyStats(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        return ResponseEntity.ok(
                myPageService.getMyPageInfo(userId)
        );
    }

    @Operation(
            summary = "로그아웃",
            description = """
                    현재 로그인한 사용자를 로그아웃 처리합니다.
                    
                    JWT 기반 인증 구조이므로,
                    프론트에서는 저장된 accessToken을 함께 제거해야 합니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @PostMapping("/api/auth/logout")
    public ResponseEntity<String> logout() {
        myPageService.logout();
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    @Operation(
            summary = "회원 탈퇴",
            description = """
                    로그인한 사용자의 계정을 삭제합니다.
                    
                    회원 탈퇴 시:
                    - 사용자 정보
                    - 루틴 정보
                    - 섭취 기록
                    - 즐겨찾기
                    
                    등 사용자 관련 데이터가 함께 삭제될 수 있습니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/api/user/withdraw")
    public ResponseEntity<Void> withdrawMember(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        myPageService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "사용자 루틴 수정",
            description = """
                    로그인한 사용자의 루틴 설정 정보를 수정합니다.
                    
                    수정 가능 정보:
                    - 평일/주말 루틴 이름
                    - 평일/주말 기상 시간
                    - 평일/주말 취침 시간
                    - 카페인 민감도
                    - 카페인 섭취 빈도
                    
                    수정된 루틴 정보는
                    메뉴 상세 화면의 위험도 계산 및
                    취침 시 예상 잔존 카페인 계산에 즉시 반영됩니다.
                    """
    )
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/api/users/me/routine")
    public ApiResTemplate<?> updateRoutine(
            Authentication authentication,
            @RequestBody RoutineRequest request
    ) {

        Long userId = Long.valueOf(authentication.getName());

        myPageService.updateMyRoutine(userId, request);

        return ApiResTemplate.successWithNoContent(
                SuccessCode.SAVE_SUCCESS
        );
    }
}