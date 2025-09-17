package com.fingrow.domain.financial.bond.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BondDto {
    
    @JsonProperty("isinCd")
    private String isinCd; // ISIN 코드
    
    @JsonProperty("isinCdNm")
    private String isinCdNm; // 종목명
    
    @JsonProperty("bondIsurNm")
    private String bondIsurNm; // 발행회사명
    
    @JsonProperty("bondSrfcInrt")
    private String bondSrfcInrt; // 금리(문자열로 받아서 변환 필요)
    
    @JsonProperty("bondExprDt")
    private String bondExprDt; // 만기일(YYYYMMDD 형식)
    
    @JsonProperty("scrsItmsKcdNm")
    private String scrsItmsKcdNm; // 유가증권종목종류코드명
    
    @JsonProperty("basDt")
    private String basDt; // 기준일
}