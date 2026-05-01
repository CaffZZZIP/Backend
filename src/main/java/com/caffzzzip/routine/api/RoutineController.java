package com.caffzzzip.routine.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.routine.api.dto.RoutineRequest;
import com.caffzzzip.routine.application.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/routine")
public class RoutineController {

    private final RoutineService routineService;

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