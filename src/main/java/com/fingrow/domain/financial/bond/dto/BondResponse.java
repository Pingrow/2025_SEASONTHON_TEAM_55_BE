package com.fingrow.domain.financial.bond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BondResponse {
    private Boolean success;
    private String message;
    private BondData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BondData {
        private List<BondDto> sortByInterest;
        private List<BondDto> sortByMaturity;
    }
}