package com.fingrow.domain.financial.bond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BondDto {
    private String bondIsurNm;
    private String isinCdNm;
    private Double bondSrfcInrt;
    private String bondExprDt;
}