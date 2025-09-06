package com.fingrow.domain.financial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "개별 상품 추천")
public class ProductRecommendation {

    @Schema(description = "상품 유형", example = "예금")
    private String productType;

    @Schema(description = "은행명", example = "우리은행")
    private String bankName;

    @Schema(description = "상품명", example = "WON플러스예금")
    private String productName;

    @Schema(description = "금리(%)", example = "2.45")
    private Double interestRate;

    @Schema(description = "기간(개월)", example = "12")
    private Integer term;

    @Schema(description = "예상 수익", example = "125000")
    private Double expectedReturn;

    @Schema(description = "투입 금액", example = "2500000")
    private Long inputAmount;

    @Schema(description = "만기 금액", example = "2625000")
    private Long maturityAmount;

    @Schema(description = "월 납입액 (적금의 경우)", example = "200000")
    private Long monthlyAmount;
}
