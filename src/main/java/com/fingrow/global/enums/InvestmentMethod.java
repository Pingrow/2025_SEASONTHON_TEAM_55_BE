package com.fingrow.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvestmentMethod {
    ONE_TIME_ONE_PLACE("한번에 한곳"),
    ONE_TIME_MULTIPLE_PLACES("한번에 여러곳"),
    MULTIPLE_TIMES_ONE_PLACE("여러번에 한곳"),
    MULTIPLE_TIMES_MULTIPLE_PLACES("여러번에 여러곳");

    private final String description;
}