package com.fingrow.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InvestmentGoal {
    RETIREMENT("은퇴 준비"),
    HOUSE_PURCHASE("내 집 마련"),
    HOME_PURCHASE("주택 구매"),
    EDUCATION("교육비 준비"),
    EMERGENCY_FUND("비상 자금"),
    WEALTH_BUILDING("자산 증대"),
    TRAVEL("여행 자금"),
    WEDDING("결혼 자금"),
    CAR_PURCHASE("자동차 구매"),
    BUSINESS_STARTUP("사업 자금"),
    BUSINESS("창업"),
    OTHER("기타");

    private final String description;
}