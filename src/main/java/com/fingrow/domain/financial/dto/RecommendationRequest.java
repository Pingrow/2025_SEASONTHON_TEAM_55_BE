package com.fingrow.domain.financial.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "상품 추천 요청")
public class RecommendationRequest {

    @NotNull(message = "목표 금액은 필수입니다.")
    @Min(value = 1000000, message = "목표 금액은 최소 30만원 이상이어야 합니다.")
    @Schema(description = "목표 금액", example = "3000000", required = true)
    private Long targetAmount;

    @NotNull(message = "목표 기간은 필수입니다.")
    @Min(value = 1, message = "목표 기간은 최소 1개월 이상이어야 합니다.")
    @Max(value = 60, message = "목표 기간은 최대 60개월까지 가능합니다.")
    @Schema(description = "목표 기간(개월)", example = "12", required = true)
    private Integer targetMonths;

    @Min(value = 0, message = "현재 보유 금액은 0원 이상이어야 합니다.")
    @Schema(description = "현재까지 모은 금액", example = "1000000")
    @Builder.Default
    private Long currentAmount = 0L;

    @Schema(description = "위험도 선호", example = "LOW", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    @Builder.Default
    private String riskPreference = "LOW";
}
