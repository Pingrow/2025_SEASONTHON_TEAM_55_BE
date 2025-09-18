package com.fingrow.domain.financial.etf.dto;

import lombok.*;
import java.util.List;

public class EtfDto {

    // ==================== API 응답 공통 DTO ====================
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private String timestamp;

        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .build();
        }

        public static <T> ApiResponse<T> success(T data) {
            return success("요청이 성공적으로 처리되었습니다.", data);
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .build();
        }
    }

    // ==================== ETF 상품+시세 정보 DTO ====================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtfProductResponse {
        // 기본 정보
        private Long id;
        private String srtnCd;           // 종목코드
        private String isinCd;           // ISIN코드
        private String itmsNm;           // 종목명 (ETF명)
        private String mrktCtg;          // 시장구분
        private String corpNm;           // 운용사명
        private String createdAt;        // String으로 저장

        // 시세 정보
        private String basDt;            // 기준일자
        private Long clpr;               // 종가
        private String vs;               // 전일대비
        private String fltRt;            // 등락률
        private Long mkp;                // 시가
        private Long hipr;               // 고가
        private Long lopr;               // 저가
        private Long trqu;               // 거래량
        private Long trPrc;              // 거래대금
        private Long lstgStCnt;          // 상장주식수
        private Long mrktTotAmt;         // 시가총액
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtfListResponse {
        private List<EtfProductResponse> etfs;
        private long totalCount;
    }

    // ==================== ETF 동기화 응답 DTO ====================
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SyncResponse {
        private String syncType;                // 동기화 타입
        private int totalProcessed;             // 처리된 총 개수
        private int successCount;               // 성공 개수
        private int failureCount;               // 실패 개수
        private List<String> failureReasons;   // 실패 사유 목록
        private String startTime;              // 동기화 시작 시간
        private String endTime;                // 동기화 종료 시간
        private String duration;               // 소요 시간
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtfListItemResponse {
        private String srtnCd;           // 종목코드 (상세조회용)
        private String itmsNm;           // ETF명
        private String corpNm;           // 운용사
        private Long clpr;               // 현재가 (종가)
        private String vs;               // 전일대비 (-40, +50 등)
        private String fltRt;            // 등락률 (-0.41%, +1.25% 등)
        private Long trqu;               // 거래량
        private Long mrktTotAmt;         // 시가총액

        // 편의 메서드들 (JSON에 노출되지 않음)
        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean isUp() {
            return vs != null && vs.startsWith("+");
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean isDown() {
            return vs != null && vs.startsWith("-");
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean isUnchanged() {
            return vs == null || (!vs.startsWith("+") && !vs.startsWith("-"));
        }

        // 숫자 포맷팅을 위한 메서드 (JSON에 노출되지 않음)
        @com.fasterxml.jackson.annotation.JsonIgnore
        public String getFormattedPrice() {
            return clpr != null ? String.format("%,d원", clpr) : "0원";
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        public String getFormattedVolume() {
            if (trqu == null) return "0";
            if (trqu >= 1_000_000) {
                return String.format("%.1f백만", trqu / 1_000_000.0);
            } else if (trqu >= 1_000) {
                return String.format("%.1f천", trqu / 1_000.0);
            }
            return String.format("%,d", trqu);
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        public String getFormattedMarketCap() {
            if (mrktTotAmt == null) return "0억";
            if (mrktTotAmt >= 1_000_000_000_000L) { // 1조 이상
                return String.format("%.1f조", mrktTotAmt / 1_000_000_000_000.0);
            } else if (mrktTotAmt >= 100_000_000L) { // 1억 이상
                return String.format("%.0f억", mrktTotAmt / 100_000_000.0);
            }
            return String.format("%,d원", mrktTotAmt);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtfSimpleListResponse {
        private List<EtfListItemResponse> etfs;
        private long totalCount;
        private String baseDate;  // 기준일자 (20250918)
    }
}