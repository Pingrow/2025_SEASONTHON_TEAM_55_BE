package com.fingrow.domain.onboard.controller;

import com.fingrow.domain.onboard.dto.OnboardDto;
import com.fingrow.domain.onboard.entity.InvestmentPreference;
import com.fingrow.domain.onboard.service.OnboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Tag(name = "온보딩 API", description = "사용자 온보딩 및 투자 성향 분석 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/onboard")
@RequiredArgsConstructor
@Validated
public class OnboardController {

    private final OnboardService onboardService;

    @Operation(summary = "설문 문항 조회", description = "온보딩 설문에 필요한 모든 문항과 선택지를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "설문 문항 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OnboardDto.SurveyQuestions.class)))
    })
    @GetMapping("/questions")
    public ResponseEntity<OnboardDto.SurveyQuestions> getSurveyQuestions() {
        log.info("Fetching survey questions");
        OnboardDto.SurveyQuestions questions = onboardService.getSurveyQuestions();
        return ResponseEntity.ok(questions);
    }

    @Operation(summary = "온보딩 설문 제출/다시하기", description = "사용자의 온보딩 설문을 제출하고 투자 성향을 분석합니다. 기존 설문이 있으면 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "설문 처리 및 분석 완료",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OnboardDto.SurveyResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/survey")
    public ResponseEntity<?> submitSurvey(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody OnboardDto.SurveyRequest request) {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "인증이 필요합니다.");
                return ResponseEntity.status(401).body(errorResponse);
            }

            String userId = (String) authentication.getPrincipal();
            log.info("Processing survey submission for user: {}", userId);

            OnboardDto.SurveyResponse response = onboardService.processSurvey(userId, request);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Survey submission failed for user", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during survey submission", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "설문 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "투자 성향 조회", description = "사용자의 투자 성향 분석 결과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "투자 성향 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "투자 성향 정보가 없음 (온보딩 미완료)")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/preference")
    public ResponseEntity<?> getInvestmentPreference(
            @Parameter(hidden = true) Authentication authentication) {

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "인증이 필요합니다.");
                return ResponseEntity.status(401).body(errorResponse);
            }

            String userId = (String) authentication.getPrincipal();
            log.info("Fetching investment preference for user: {}", userId);

            Optional<InvestmentPreference> preference = onboardService.getUserInvestmentPreference(userId);

            if (preference.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("completed", false);
                response.put("message", "온보딩을 완료해주세요.");
                return ResponseEntity.ok(response);
            }

            InvestmentPreference pref = preference.get();
            Map<String, Object> response = new HashMap<>();
            response.put("completed", true);
            response.put("riskLevel", pref.getRiskLevel());
            response.put("investmentGoal", pref.getInvestmentGoal());
            response.put("targetAmount", pref.getTargetAmount());
            response.put("minInvestmentPeriod", pref.getMinInvestmentPeriod());
            response.put("maxInvestmentPeriod", pref.getMaxInvestmentPeriod());
            response.put("preferredInvestmentTypes", pref.getPreferredInvestmentTypes());
            response.put("investmentMethod", pref.getInvestmentMethod());
            response.put("lossTolerance", pref.getLossTolerance());
            response.put("address", pref.getAddress());
            response.put("createdAt", pref.getCreatedAt());
            response.put("updatedAt", pref.getUpdatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching investment preference", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "투자 성향 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }


}