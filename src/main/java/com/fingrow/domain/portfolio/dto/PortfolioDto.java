package com.fingrow.domain.portfolio.dto;

import lombok.*;

import java.util.Map;

public class PortfolioDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendResponse {
        private String riskLevel;
        private Long targetAmount;
        private Integer investmentPeriod;
        private Map<String, Integer> allocation;
        private Double expectedTotal;
        private Map<String, Object> recommendedProducts;
        private String gptReasoning;
    }
}
