package com.fingrow.domain.onboard.dto;

import com.fingrow.global.enums.InvestmentGoal;
import com.fingrow.global.enums.InvestmentPeriod;
import com.fingrow.global.enums.PreferredInvestmentType;
import com.fingrow.global.enums.RiskLevel;
import com.fingrow.global.enums.InvestmentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Set;

public class OnboardDto {

    @Schema(description = "온보딩 설문 요청")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurveyRequest {

        @NotNull(message = "투자 방식을 선택해주세요")
        @Schema(description = "투자 방식", example = "LUMP_SUM")
        private InvestmentMethod investmentMethod;

        @NotNull(message = "손실 감내 수준을 선택해주세요")
        @Schema(description = "손실 감내 수준 (위험 수준)", example = "MODERATE")
        private RiskLevel riskLevel;

        @NotEmpty(message = "투자 유형을 하나 이상 선택해주세요")
        @Schema(description = "선호하는 투자 유형들", example = "[\"SAVINGS\", \"ETF\"]")
        private Set<PreferredInvestmentType> preferredInvestmentTypes;

        @NotNull(message = "투자 목표 기간을 선택해주세요")
        @Schema(description = "투자 목표 기간", example = "MEDIUM_TERM")
        private InvestmentPeriod investmentPeriod;

        @NotNull(message = "투자 목표를 선택해주세요")
        @Schema(description = "투자 목표", example = "WEALTH_BUILDING")
        private InvestmentGoal investmentGoal;

        @NotNull(message = "목표 금액을 입력해주세요")
        @DecimalMin(value = "0", message = "목표 금액은 0보다 커야 합니다")
        @Schema(description = "목표 금액", example = "10000000")
        private BigDecimal targetAmount;

        @Schema(description = "월 투자 금액", example = "500000")
        @DecimalMin(value = "0", message = "월 투자 금액은 0보다 커야 합니다")
        private BigDecimal monthlyInvestmentAmount;

        @Schema(description = "투자 경험", example = "주식 투자 1년차")
        @Size(max = 500, message = "투자 경험은 500자 이내로 입력해주세요")
        private String currentInvestmentExperience;

        @Schema(description = "추가 메모", example = "안전한 투자를 선호합니다")
        @Size(max = 1000, message = "추가 메모는 1000자 이내로 입력해주세요")
        private String additionalNotes;
    }

    @Schema(description = "온보딩 설문 응답")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurveyResponse {
        @Schema(description = "설문 완료 여부", example = "true")
        private boolean completed;

        @Schema(description = "응답 메시지", example = "투자 성향 분석이 완료되었습니다.")
        private String message;

        @Schema(description = "투자 성향 분석 결과")
        private InvestmentAnalysis analysis;
    }

    @Schema(description = "투자 성향 분석 결과")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestmentAnalysis {
        @Schema(description = "투자자 성향", example = "안정형")
        private String investorProfile;

        @Schema(description = "위험 수준", example = "MODERATE")
        private RiskLevel riskLevel;

        @Schema(description = "추천 투자 상품")
        private RecommendedProduct recommendedProduct;

        @Schema(description = "투자 성향 설명",
                example = "안정적인 수익을 추구하며 적당한 위험을 감수할 수 있는 투자자입니다.")
        private String description;
    }

    @Schema(description = "추천 투자 상품")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedProduct {
        @Schema(description = "추천 상품 유형", example = "ETF")
        private PreferredInvestmentType productType;

        @Schema(description = "추천 상품명", example = "주식형 ETF")
        private String productName;

        @Schema(description = "추천 이유",
                example = "중기 투자 목표와 적당한 위험 감수 성향에 적합한 상품입니다.")
        private String reason;

        @Schema(description = "예상 수익률", example = "연 5-7%")
        private String expectedReturn;

        @Schema(description = "위험도", example = "중간")
        private String riskDescription;
    }


    @Schema(description = "설문 문항 정보")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurveyQuestions {
        @Schema(description = "투자 방식 선택지")
        private InvestmentMethodOption[] investmentMethods;

        @Schema(description = "손실 감내 수준 선택지")
        private RiskLevelOption[] riskLevels;

        @Schema(description = "투자 유형 선택지")
        private InvestmentTypeOption[] investmentTypes;

        @Schema(description = "투자 기간 선택지")
        private PeriodOption[] investmentPeriods;

        @Schema(description = "투자 목표 선택지")
        private GoalOption[] investmentGoals;
    }

    // 각 선택지의 상세 정보를 위한 클래스들
    @Data
    @AllArgsConstructor
    public static class InvestmentMethodOption {
        private InvestmentMethod value;
        private String name;
        private String description;
    }

    @Data
    @AllArgsConstructor
    public static class RiskLevelOption {
        private RiskLevel value;
        private String name;
        private String description;
        private String lossRange;
    }

    @Data
    @AllArgsConstructor
    public static class InvestmentTypeOption {
        private PreferredInvestmentType value;
        private String name;
        private String description;
    }

    @Data
    @AllArgsConstructor
    public static class PeriodOption {
        private InvestmentPeriod value;
        private String name;
        private String description;
    }

    @Data
    @AllArgsConstructor
    public static class GoalOption {
        private InvestmentGoal value;
        private String name;
        private String description;
    }
}