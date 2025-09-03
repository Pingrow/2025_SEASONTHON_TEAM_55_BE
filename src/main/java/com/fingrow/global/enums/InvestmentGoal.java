package com.fingrow.global.enums;

public enum InvestmentGoal {
    EMERGENCY_FUND("비상자금 마련"),
    WEALTH_BUILDING("자산 증식"),
    RETIREMENT("노후 준비"),
    HOME_PURCHASE("주택 마련"),
    EDUCATION("교육비 준비"),
    TRAVEL("여행 자금"),
    BUSINESS("창업 자금"),
    OTHER("기타");

    private final String description;

    InvestmentGoal(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}