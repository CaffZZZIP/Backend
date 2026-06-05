package com.caffzzzip.menu.api;

import com.caffzzzip.common.error.SuccessCode;
import com.caffzzzip.common.template.ApiResTemplate;
import com.caffzzzip.menu.api.dto.BrandResponse;
import com.caffzzzip.menu.api.dto.MenuDetailResponse;
import com.caffzzzip.menu.api.dto.MenuResponse;
import com.caffzzzip.menu.application.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
@Tag(name = "Menu", description = "카페인 메뉴 API")
public class MenuController {

    private final MenuService menuService;

    @Operation(
            summary = "브랜드 목록 조회",
            description = """
                    등록된 활성 메뉴 데이터를 기준으로 브랜드 목록을 조회합니다.

                    프론트 사용 위치:
                    - 섭취 기록 첫 화면의 브랜드 선택 영역

                    반환 예시:
                    - 스타벅스
                    - 이디야
                    - 할리스
                    - 메가커피
                    - 빽다방
                    """
    )
    @GetMapping("/brands")
    public ApiResTemplate<List<BrandResponse>> getBrands() {
        List<BrandResponse> response = menuService.getBrands();
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }

    @Operation(
            summary = "카테고리별 메뉴 목록 조회",
            description = """
                    특정 카테고리에 속한 메뉴 목록을 조회합니다.

                    프론트 사용 위치:
                    - 브랜드 선택 후 메뉴 리스트 화면
                    - 카테고리 탭 선택 시 해당 카테고리 메뉴 조회

                    반환 정보:
                    - 메뉴 ID
                    - 메뉴명
                    - 브랜드명
                    - 카테고리명
                    - 메뉴 1개 기준 카페인 함량

                    참고:
                    - 조회 결과가 없으면 에러가 아니라 빈 배열이 반환됩니다.
                    """
    )
    @GetMapping("/category/{categoryId}")
    public ApiResTemplate<List<MenuResponse>> getMenusByCategory(
            @Parameter(description = "카테고리 ID", example = "1")
            @PathVariable Long categoryId
    ) {
        List<MenuResponse> response = menuService.getMenusByCategory(categoryId);
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }

    @Operation(
            summary = "브랜드/카테고리 조건 메뉴 목록 조회",
            description = """
                    브랜드명과 카테고리 ID를 조건으로 메뉴 목록을 조회합니다.

                    프론트 사용 위치:
                    - 브랜드 선택 후 해당 브랜드의 전체 메뉴 조회
                    - 브랜드 내부에서 카테고리 탭을 선택했을 때 메뉴 필터링
                    - 전체 메뉴 목록 조회

                    요청 조건:
                    - brand와 categoryId는 선택값입니다.
                    - brand만 전달하면 해당 브랜드의 전체 메뉴를 조회합니다.
                    - categoryId만 전달하면 해당 카테고리의 전체 메뉴를 조회합니다.
                    - brand와 categoryId를 함께 전달하면 해당 브랜드의 특정 카테고리 메뉴를 조회합니다.
                    - 아무 조건도 전달하지 않으면 활성화된 전체 메뉴 목록을 조회합니다.

                    예시:
                    - GET /api/menus?brand=스타벅스
                    - GET /api/menus?categoryId=1
                    - GET /api/menus?brand=스타벅스&categoryId=1

                    참고:
                    - brand 값은 DB에 저장된 한글 브랜드명을 그대로 전달해야 합니다.
                    - 예: 스타벅스, 이디야, 할리스, 메가커피, 빽다방
                    - 조회 결과가 없으면 에러가 아니라 빈 배열이 반환됩니다.
                    """
    )
    @GetMapping
    public ApiResTemplate<List<MenuResponse>> getMenus(
            @Parameter(description = "브랜드명", example = "스타벅스")
            @RequestParam(required = false) String brand,

            @Parameter(description = "카테고리 ID", example = "1")
            @RequestParam(required = false) Long categoryId
    ) {
        List<MenuResponse> response = menuService.getMenus(brand, categoryId);
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }

    @Operation(
            summary = "메뉴 검색",
            description = """
                    메뉴명을 기준으로 카페인 메뉴를 검색합니다.

                    프론트 사용 위치:
                    - 메뉴 리스트 화면 상단 검색창
                    - 특정 브랜드 내 메뉴 검색

                    요청 조건:
                    - keyword는 필수값입니다.
                    - brand는 선택값입니다.
                    - brand를 함께 전달하면 해당 브랜드 내에서만 메뉴명을 검색합니다.
                    - brand를 전달하지 않으면 전체 브랜드 메뉴를 대상으로 검색합니다.

                    예시:
                    - GET /api/menus/search?keyword=아메리카노
                    - GET /api/menus/search?keyword=아메리카노&brand=스타벅스

                    참고:
                    - brand 값은 DB에 저장된 한글 브랜드명을 그대로 전달해야 합니다.
                    - 예: 스타벅스, 이디야, 할리스, 메가커피, 빽다방
                    - 검색 결과가 없으면 에러가 아니라 빈 배열이 반환됩니다.
                    """
    )
    @GetMapping("/search")
    public ApiResTemplate<List<MenuResponse>> searchMenus(
            @Parameter(description = "검색어", example = "아메리카노")
            @RequestParam String keyword,

            @Parameter(description = "브랜드명", example = "스타벅스")
            @RequestParam(required = false) String brand
    ) {
        List<MenuResponse> response = menuService.searchMenus(keyword, brand);
        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }

    @Operation(
            summary = "메뉴 상세 조회 및 섭취 전 카페인 위험도 계산",
            description = """
                    메인에서 선택한 오늘 루틴 모드가 있는 경우, 실제 요일보다 선택한 루틴 모드를 우선 적용합니다.
                    선택된 값이 없으면 현재 날짜 기준으로 평일/주말을 자동 판단합니다.
                    
                    사용자가 특정 메뉴를 실제 섭취 기록으로 저장하기 전에,
                    해당 메뉴를 마셨을 경우의 예상 카페인 상태를 미리 계산합니다.

                    이 API는 섭취 기록 저장 API가 아닙니다.
                    실제 저장은 POST /api/intake에서 수행합니다.

                    이 API의 목적은 메뉴 상세 화면에서
                    사용자가 섭취 시간과 수량을 선택하는 동안
                    금일 예상 섭취량, 위험도, 취침 시 잔존 카페인량, 안내 문구를
                    실시간으로 보여주기 위한 것입니다.

                    프론트 사용 위치:
                    - 메뉴 상세 화면 진입 시 호출
                    - 사용자가 섭취 시간(intakeAt)을 변경할 때 재호출
                    - 사용자가 수량(quantity)을 변경할 때 재호출
                    - 응답값으로 금일 섭취량 바, 안전/주의/위험 딱지, 안내 문구를 갱신

                    요청 파라미터:
                    - menuId: 상세 조회할 메뉴 ID
                    - intakeAt: 사용자가 선택한 섭취 예정 시간
                      값을 보내지 않으면 현재 시간 기준으로 계산합니다.
                    - quantity: 사용자가 선택한 수량
                      값을 보내지 않으면 1개 기준으로 계산합니다.

                    주요 응답값 설명:

                    1. caffeineMg
                    - 메뉴 1개 기준 카페인 함량입니다.

                    2. quantity
                    - 프론트에서 전달한 선택 수량입니다.

                    3. intakeCaffeine
                    - 현재 선택한 메뉴를 quantity만큼 마신다고 가정했을 때의 카페인량입니다.
                    - 계산식: caffeineMg × quantity

                    4. todayTotalCaffeine
                    - 오늘 이미 저장된 섭취 기록들의 총 카페인량입니다.
                    - 현재 상세 화면에서 선택 중인 메뉴는 아직 포함하지 않습니다.

                    5. expectedTotalCaffeine
                    - 오늘 기존 섭취량에 현재 선택 중인 메뉴의 카페인량을 더한 예상 총 섭취량입니다.
                    - 금일 섭취량 바와 위험도 딱지 판단 기준입니다.
                    - 계산식: todayTotalCaffeine + intakeCaffeine

                    6. dailyRecommendedLimit
                    - 하루 권장 카페인 기준량입니다.
                    - 현재 기준값은 400mg입니다.
                    - 프론트는 expectedTotalCaffeine / dailyRecommendedLimit 기준으로 진행 바를 표시하면 됩니다.

                    7. riskLevel
                    - 위험도 enum 값입니다.
                    - SAFE, CAUTION, DANGER 중 하나입니다.
                    - 프론트에서 색상 분기나 상태 분기용으로 사용합니다.

                    8. riskLabel
                    - 화면에 표시할 위험도 한글 문구입니다.
                    - 안전, 주의, 위험 중 하나입니다.

                    9. caffeineSensitivity
                    - 사용자의 카페인 민감도입니다.
                    - LOW, NORMAL, HIGH 중 하나입니다.
                    - 사용자가 초기 루틴 설정에서 입력한 값입니다.

                    10. expectedRemainingCaffeine
                    - 사용자의 취침 시간 기준 예상 잔존 카페인량입니다.
                    - 오늘 이미 저장된 섭취 기록들의 잔존 카페인량과
                      현재 선택 중인 메뉴의 잔존 카페인량을 모두 합산한 값입니다.
                    - 카페인 반감기는 사용자 민감도 기준으로 계산합니다.
                      LOW: 약 4시간
                      NORMAL: 약 5시간
                      HIGH: 약 6시간

                    11. guideMessage
                    - 화면 하단에 표시할 섭취 전 안내 문구입니다.
                    - 사용자 민감도, 섭취 예정 시간, 취침 시간,
                      expectedRemainingCaffeine, riskLevel을 기반으로 생성됩니다.

                    화면 적용 방식:
                    - 금일 섭취량 바: expectedTotalCaffeine / dailyRecommendedLimit
                    - 위험도 딱지: riskLabel 표시
                    - 위험도 색상: riskLevel 기준 분기
                    - 하단 안내 문구: guideMessage 표시

                    전체 흐름:
                    1. 사용자가 메뉴 목록에서 메뉴를 선택합니다.
                    2. 프론트는 GET /api/menus/{menuId}를 호출해 상세 정보를 조회합니다.
                    3. 사용자가 섭취 시간 또는 수량을 변경하면
                       GET /api/menus/{menuId}?intakeAt=...&quantity=... 형태로 다시 호출합니다.
                    4. 응답값으로 화면의 바, 딱지, 안내 문구를 갱신합니다.
                    5. 사용자가 기록에 추가하기 버튼을 누르면 POST /api/intake를 호출해 실제 섭취 기록을 저장합니다.

                    정리:
                    - GET /api/menus/{menuId}: 섭취 전 예상 계산 및 상세 화면 표시용
                    - POST /api/intake: 실제 섭취 기록 저장용
                    """
    )
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{menuId}")
    public ApiResTemplate<MenuDetailResponse> getMenuDetail(
            Authentication authentication,

            @Parameter(description = "상세 조회할 메뉴 ID", example = "1")
            @PathVariable Long menuId,

            @Parameter(
                    description = "섭취 예정 시간입니다. 값을 전달하지 않으면 현재 시간 기준으로 계산합니다.",
                    example = "2026-05-20T14:10:00"
            )
            @RequestParam(required = false) LocalDateTime intakeAt,

            @Parameter(
                    description = "섭취 예정 수량입니다. 값을 전달하지 않으면 1개 기준으로 계산합니다.",
                    example = "1"
            )
            @RequestParam(required = false) Integer quantity
    ) {
        Long userId = Long.valueOf(authentication.getName());

        MenuDetailResponse response = menuService.getMenuDetail(
                userId,
                menuId,
                intakeAt,
                quantity
        );

        return ApiResTemplate.successResponse(SuccessCode.GET_SUCCESS, response);
    }
}