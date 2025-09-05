package com.fingrow.domain.financial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "조합 상품 정보")
public class CombinationProduct {
    @Schema(description = "상품 유형", example = "예금", allowableValues = {"예금", "적금"})
    private String productType;

    @Schema(description = "회사명", example = "우리은행")
    private String bankName;

    @Schema(description = "상품명", example = "WON플러스예금")
    private String productName;

    @Schema(description = "기간(개월)", example = "12")
    private Integer term;

    @Schema(description = "금리(%)", example = "2.45")
    private Double interestRate;

    @Schema(description = "우대 조건")
    private String specialCondition;

    // 예금인 경우만
    @Schema(description = "투입 금액", example = "2500000")
    private Long depositAmount;

    @Schema(description = "만기 금액", example = "2580000")
    private Long maturityAmount;

    // 적금인 경우만
    @Schema(description = "월 납입 금액", example = "208333")
    private Long monthlyAmount;

    @Schema(description = "총 적립 목표 금액", example = "2500000")
    private Long totalSavingAmount;

    @Schema(description = "적금 만기 예상 금액", example = "2590000")
    private Long savingMaturityAmount;

    // 공통
    @Schema(description = "예상 수익", example = "80000")
    private Double expectedReturn;
}

