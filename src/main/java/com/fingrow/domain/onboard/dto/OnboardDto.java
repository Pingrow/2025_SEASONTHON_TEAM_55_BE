package com.fingrow.domain.onboard.dto;

import com.fingrow.global.enums.InvestmentGoal;
import com.fingrow.global.enums.InvestmentMethod;
import com.fingrow.global.enums.InvestmentPeriod;
import com.fingrow.global.enums.PreferredInvestmentType;
import com.fingrow.global.enums.RiskLevel;
import com.fingrow.global.enums.LossTolerance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class OnboardDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnboardRequest {
        private RiskLevel riskLevel;
        private InvestmentGoal investmentGoal;
        private BigDecimal targetAmount;
        private InvestmentPeriod investmentPeriod;
        private Set<PreferredInvestmentType> preferredInvestmentTypes;
        private BigDecimal monthlyInvestmentAmount;
        private String currentInvestmentExperience;
        private String additionalNotes;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnboardResponse {
        private Long id;
        private Long userId;
        private RiskLevel riskLevel;
        private InvestmentGoal investmentGoal;
        private BigDecimal targetAmount;
        private InvestmentPeriod investmentPeriod;
        private Set<PreferredInvestmentType> preferredInvestmentTypes;
        private BigDecimal monthlyInvestmentAmount;
        private String currentInvestmentExperience;
        private String additionalNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurveyRequest {
        private InvestmentMethod investmentMethod;           // 한번에 한곳/여러곳, 여러번에 한곳/여러곳
        private LossTolerance lossTolerance;                // 손실 감내도 (못함, 10%, 20-30%, 절반이상)
        private Set<PreferredInvestmentType> preferredInvestmentTypes;  // 예적금, ETF, 국채, 펀드 (중복선택)
        private Integer minInvestmentPeriod;                // 최소 투자 기간 (개월)
        private Integer maxInvestmentPeriod;                // 최대 투자 기간 (개월)
        private InvestmentGoal investmentGoal;              // 목표
        private BigDecimal targetAmount;                    // 필요금액
        private String address;                             // 사용자 주소
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurveyResponse {
        private InvestmentAnalysis analysis;
        private List<RecommendedProduct> recommendedProducts;
        private String message;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SurveyQuestions {
        private InvestmentMethodOption[] investmentMethodOptions;   // 투자 방식 4가지
        private LossToleranceOption[] lossToleranceOptions;         // 손실 감내도 4가지
        private InvestmentTypeOption[] investmentTypeOptions;       // 선호 투자 유형 4가지
        private GoalOption[] goalOptions;                           // 목표 선택지들
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestmentAnalysis {
        private String riskProfile;
        private String investmentStrategy;
        private String expectedReturn;
        private String recommendation;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedProduct {
        private String productType;
        private String productName;
        private String bankName;
        private BigDecimal interestRate;
        private String description;
        private String reason;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskLevelOption {
        private RiskLevel value;
        private String label;
        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalOption {
        private InvestmentGoal value;
        private String label;
        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodOption {
        private InvestmentPeriod value;
        private String label;
        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestmentTypeOption {
        private PreferredInvestmentType value;
        private String label;
        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestmentMethodOption {
        private InvestmentMethod value;
        private String label;
        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LossToleranceOption {
        private LossTolerance value;
        private String label;
        private String description;
    }
}