package com.fingrow.domain.financial.deposit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "최적 조합")
public class OptimalCombination {

    @Schema(description = "추천 상품 리스트")
    private List<CombinationProduct> products;

    @Schema(description = "조합 요약", example = "월 35만원 · 12개월 · 적금+채권 혼합")
    private String combinationSummary;

    @Schema(description = "총 예상 수익", example = "150000")
    private Double totalExpectedReturn;

    @Schema(description = "예상 총 금액", example = "5150000")
    private Double expectedTotalAmount;

    @Schema(description = "위험도", example = "낮음")
    private String riskLevel;

    @Schema(description = "조합 설명")
    private String description;
}
