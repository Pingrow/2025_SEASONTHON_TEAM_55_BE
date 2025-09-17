package com.fingrow.domain.financial.bond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BondSearchResponse {
    private String keyword;
    private List<BondSummaryDto> bonds;
    private Integer totalCount;
    private String sortBy;
}