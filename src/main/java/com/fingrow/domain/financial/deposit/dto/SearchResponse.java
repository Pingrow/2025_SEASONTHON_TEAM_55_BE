package com.fingrow.domain.financial.deposit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "상품 검색 응답")
public class SearchResponse {
    private String keyword;
    // 예금 + 적금 통합 리스트
    private List<ProductSummaryDto> products;
    private Integer totalCount;
}
