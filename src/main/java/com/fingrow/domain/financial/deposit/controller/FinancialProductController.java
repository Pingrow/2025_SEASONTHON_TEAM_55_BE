package com.fingrow.domain.financial.deposit.controller;

import com.fingrow.domain.financial.deposit.dto.*;
import com.fingrow.domain.financial.deposit.service.FinancialProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/financial")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "금융상품 API", description = "예금, 적금 상품 관리 및 추천 API")
public class FinancialProductController {

    private final FinancialProductService financialProductService;

    // ========================== 데이터 동기화 API ==========================

    @PostMapping("/sync/all")
    @Operation(
            summary = "전체 상품 데이터 동기화 (비동기)",
            description = "예금과 적금 상품 데이터를 병렬로 동기화합니다. 배치 최적화로 빠른 처리가 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동기화 시작됨"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CommonResponse<String>> syncAllProducts() {
        try {
            log.info("전체 상품 데이터 동기화 요청 (비동기)");

            // 비동기 동기화 시작
            financialProductService.syncAllProductsAsync();

            return ResponseEntity.ok(
                    CommonResponse.success("전체 상품 데이터 동기화가 시작되었습니다. 백그라운드에서 처리중입니다.")
            );
        } catch (Exception e) {
            log.error("전체 상품 데이터 동기화 실패", e);
            return ResponseEntity.internalServerError().body(
                    CommonResponse.error("전체 상품 데이터 동기화에 실패했습니다: " + e.getMessage())
            );
        }
    }

    @PostMapping("/sync/all/sync")
    @Operation(
            summary = "전체 상품 데이터 동기화 (동기)",
            description = "예금과 적금 상품 데이터를 순차적으로 동기화합니다. 완료까지 대기합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동기화 완료"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CommonResponse<String>> syncAllProductsSync() {
        try {
            log.info("전체 상품 데이터 동기화 요청 (동기)");
            financialProductService.syncDepositProducts();
            financialProductService.syncSavingProducts();
            return ResponseEntity.ok(
                    CommonResponse.success("전체 상품 데이터 동기화가 완료되었습니다.")
            );
        } catch (Exception e) {
            log.error("전체 상품 데이터 동기화 실패", e);
            return ResponseEntity.internalServerError().body(
                    CommonResponse.error("전체 상품 데이터 동기화에 실패했습니다: " + e.getMessage())
            );
        }
    }

    // ========================== 상품 조회 API ==========================

    @GetMapping("/deposits")
    public ResponseEntity<CommonResponse<List<ProductSummaryDto>>> getAllDepositProducts() {
        try {
            List<ProductSummaryDto> products = financialProductService.getAllDepositProductsSummary();
            return ResponseEntity.ok(
                    CommonResponse.success(
                            products.size() + "개의 예금 상품을 조회했습니다.",
                            products
                    )
            );
        } catch (Exception e) {
            log.error("예금 상품 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    CommonResponse.error("예금 상품 조회에 실패했습니다: " + e.getMessage())
            );
        }
    }

    @GetMapping("/savings")
    public ResponseEntity<CommonResponse<List<ProductSummaryDto>>> getAllSavingProducts() {
        try {
            List<ProductSummaryDto> products = financialProductService.getAllSavingProductsSummary();
            return ResponseEntity.ok(
                    CommonResponse.success(
                            products.size() + "개의 적금 상품을 조회했습니다.",
                            products
                    )
            );
        } catch (Exception e) {
            log.error("적금 상품 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    CommonResponse.error("적금 상품 조회에 실패했습니다: " + e.getMessage())
            );
        }
    }

    // ========================== 상품 검색 API ==========================

    @GetMapping("/search")
    @Operation(
            summary = "상품 검색",
            description = "상품명이나 은행명으로 예금/적금 상품을 검색합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CommonResponse<SearchResponse>> searchProducts(
            @Parameter(description = "검색 키워드", example = "우리은행", required = true)
            @RequestParam @NotBlank(message = "검색 키워드는 필수입니다.") String keyword) {
        try {
            log.info("상품 검색 요청: {}", keyword);
            SearchResponse searchResult = financialProductService.searchProducts(keyword);
            return ResponseEntity.ok(
                    CommonResponse.success(
                            "'" + keyword + "'로 " + searchResult.getTotalCount() + "개 상품을 찾았습니다.",
                            searchResult
                    )
            );
        } catch (Exception e) {
            log.error("상품 검색 실패", e);
            return ResponseEntity.internalServerError().body(
                    CommonResponse.error("상품 검색에 실패했습니다: " + e.getMessage())
            );
        }
    }

    // ========================== 상품 추천 API ==========================

    @PostMapping("/recommend")
    @Operation(
            summary = "맞춤형 상품 추천",
            description = "목표 금액과 기간을 기반으로 최적의 예적금 상품을 추천합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CommonResponse<RecommendationResponse>> recommendProducts(
            @Parameter(description = "추천 요청 정보", required = true)
            @RequestBody @Valid RecommendationRequest request) {
        try {
            log.info("상품 추천 요청: 목표금액={}, 기간={}개월", request.getTargetAmount(), request.getTargetMonths());
            RecommendationResponse recommendation = financialProductService.recommendProducts(request);
            return ResponseEntity.ok(
                    CommonResponse.success(
                            recommendation.getTotalProducts() + "개의 상품을 추천합니다.",
                            recommendation
                    )
            );
        } catch (Exception e) {
            log.error("상품 추천 실패", e);
            return ResponseEntity.internalServerError().body(
                    CommonResponse.error("상품 추천에 실패했습니다: " + e.getMessage())
            );
        }
    }

    // ========================== API 상태 체크 ==========================

    @GetMapping("/health")
    @Operation(
            summary = "API 상태 확인",
            description = "API 서버의 상태를 확인합니다."
    )
    public ResponseEntity<CommonResponse<Object>> healthCheck() {
        Object health = new Object() {
            public final String status = "UP";
            public final String timestamp = java.time.LocalDateTime.now().toString();
            public final String version = "1.0.0";
        };

        return ResponseEntity.ok(
                CommonResponse.success("API 서버가 정상 작동 중입니다.", health)
        );
    }
}