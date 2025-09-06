package com.fingrow.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvestmentPeriod {
    ONE_MONTH("1개월"),
    FIVE_YEARS_PLUS("5년이상");

    private final String description;
}