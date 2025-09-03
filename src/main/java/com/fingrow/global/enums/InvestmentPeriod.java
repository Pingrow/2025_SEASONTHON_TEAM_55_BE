package com.fingrow.global.enums;

public enum InvestmentPeriod {
    SHORT_TERM("단기", "1년 이하"),
    MEDIUM_TERM("중기", "1-3년"),
    LONG_TERM("장기", "3-5년"),
    VERY_LONG_TERM("초장기", "5년 이상");

    private final String name;
    private final String description;

    InvestmentPeriod(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}