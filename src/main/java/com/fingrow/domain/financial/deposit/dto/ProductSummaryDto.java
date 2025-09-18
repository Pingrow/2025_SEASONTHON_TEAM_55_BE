package com.fingrow.domain.financial.deposit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSummaryDto {
    private Long id;
    private String bankName;     // korCoNm
    private String productName;  // finPrdtNm
    private String productType;  // "예금" or "적금"
    private Double bestRate;     // 최고 금리
    private Integer bestTerm;    // 최고 조건의 기간(개월)
}
