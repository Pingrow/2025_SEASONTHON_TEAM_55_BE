package com.fingrow.global.enums;

public enum InvestmentMethod {
    LUMP_SUM("한번에 한 곳", "목표 금액을 한 번에 투자"),
    REGULAR("정기적으로 한 곳", "매월 일정 금액을 한 곳에 투자"),
    MIXED("여러 번에 걸쳐서 한 곳", "분할하여 한 곳에 투자"),
    FLEXIBLE("여러 번에 걸쳐서 여러 곳", "분할하여 여러 곳에 분산 투자");

    private final String name;
    private final String description;

    InvestmentMethod(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}