package com.fingrow.domain.financial.deposit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "금융감독원 적금 API 응답")
public class SavingApiResponse {

    @JsonProperty("result")
    private Result result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("baseList")
        private List<SavingProductDto> baseList;

        @JsonProperty("optionList")
        private List<SavingOptionDto> optionList;
    }
}
