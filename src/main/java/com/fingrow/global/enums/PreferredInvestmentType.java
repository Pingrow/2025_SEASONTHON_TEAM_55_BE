package com.fingrow.global.enums;

public enum PreferredInvestmentType {
    SAVINGS("예금/적금", "안전한 원금보장 상품"),
    ETF("ETF", "상장지수펀드"),
    BONDS("채권", "국채, 회사채 등"),
    FUNDS("펀드", "뮤추얼펀드, 주식형펀드 등");

    private final String name;
    private final String description;

    PreferredInvestmentType(String name, String description) {
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