package com.caffzzzip.my_page.api;

import com.caffzzzip.my_page.api.dto.MyPageResponse;
import com.caffzzzip.my_page.api.dto.MyPageRoutineResponse;
import com.caffzzzip.my_page.application.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    // 사용자 루틴 조회
    @GetMapping("/api/users/me/routine")
    public ResponseEntity<MyPageRoutineResponse> getMyRoutine(
                                                               Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());


        return ResponseEntity.ok(
                myPageService.getMyRoutineInfo(userId)
        );
    }

    // 주간 카페인 통계 조회
    @GetMapping("/api/report/stats")
    public ResponseEntity<MyPageResponse> getWeeklyStats(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        return ResponseEntity.ok(
                myPageService.getMyPageInfo(userId)
        );
    }

    // 로그아웃
    @PostMapping("/api/auth/logout")
    public ResponseEntity<String> logout() {
        myPageService.logout();
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 회원 탈퇴
    @DeleteMapping("/api/user/withdraw")
    public ResponseEntity<Void> withdrawMember(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());
        myPageService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // 사용자 루틴 수정 API
    @PatchMapping("/api/users/me/routine")
    public com.caffzzzip.common.template.ApiResTemplate<?> updateRoutine(
            org.springframework.security.core.Authentication authentication,
            @RequestBody com.caffzzzip.routine.api.dto.RoutineRequest request
    ) {

        Long userId = Long.valueOf(authentication.getName());


        myPageService.updateMyRoutine(userId, request);


        return com.caffzzzip.common.template.ApiResTemplate.successWithNoContent(
                com.caffzzzip.common.error.SuccessCode.SAVE_SUCCESS
        );
    }
}