package com.fingrow.domain.financial.bond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BondSummaryDto {
    private Long id;
    private String isinCd;
    private String bondName;
    private String issuerName;
    private Double interestRate;
    private LocalDate maturityDate;
    private String bondType;
    private Integer daysToMaturity;
    private boolean isMatured;
}