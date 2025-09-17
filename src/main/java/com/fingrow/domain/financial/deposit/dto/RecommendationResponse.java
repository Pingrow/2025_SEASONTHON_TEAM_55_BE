package com.fingrow.domain.financial.deposit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "상품 추천 응답")
public class RecommendationResponse {

    @Schema(description = "목표 금액")
    private Long targetAmount;

    @Schema(description = "목표 기간(개월)")
    private Integer targetMonths;

    @Schema(description = "추천 상품 목록")
    private List<ProductRecommendation> recommendations;

    @Schema(description = "최적 조합")
    private OptimalCombination optimalCombination;

    @Schema(description = "총 상품 수")
    private Integer totalProducts;
}
