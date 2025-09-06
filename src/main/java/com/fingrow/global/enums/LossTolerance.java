package com.fingrow.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LossTolerance {
    NONE("손실 감내 못함"),
    TEN_PERCENT("10%"),
    TWENTY_TO_THIRTY_PERCENT("20~30%"),
    HALF_OR_MORE("절반이상");

    private final String description;
}