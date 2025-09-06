package com.fingrow.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RiskLevel {
    STABLE("안정형"),
    STABILITY_SEEKING("안정추구형"),
    RISK_NEUTRAL("위험중립형"),
    ACTIVE_INVESTMENT("적극투자형"),
    AGGRESSIVE_INVESTMENT("공격투자형");

    private final String description;
}