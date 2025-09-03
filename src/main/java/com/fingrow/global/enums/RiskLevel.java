package com.fingrow.global.enums;

public enum RiskLevel {
    CONSERVATIVE("안전형"),       // 원금보장 중시
    MODERATE("안정형"),          // 적당한 위험 감수
    BALANCED("균형형"),          // 위험과 수익의 균형
    AGGRESSIVE("적극형"),        // 높은 수익 추구
    SPECULATIVE("공격형");       // 고위험 고수익 추구

    private final String description;

    RiskLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}