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
public class BondTopResponse {
    private List<BondSummaryDto> topByInterestRate;
    private List<BondSummaryDto> topByMaturity;
    private String bondType;
    private Integer topCount;
    private String syncDate;
}