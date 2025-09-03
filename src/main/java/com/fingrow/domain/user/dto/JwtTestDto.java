package com.fingrow.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

public class JwtTestDto {

    @Schema(description = "테스트 토큰 생성 요청")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateTokenRequest {
        @Schema(description = "사용자 ID", example = "1")
        private String userId;
    }

    @Schema(description = "테스트 토큰 생성 응답")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateTokenResponse {
        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String accessToken;
        
        @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String refreshToken;
        
        @Schema(description = "사용자 ID", example = "1")
        private String userId;
        
        @Schema(description = "응답 메시지", example = "테스트용 JWT 토큰이 생성되었습니다.")
        private String message;
    }

    @Schema(description = "보호된 엔드포인트 응답")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProtectedEndpointResponse {
        @Schema(description = "응답 메시지", example = "인증된 사용자만 접근 가능한 API입니다.")
        private String message;
        
        @Schema(description = "사용자 ID", example = "1")
        private String userId;
        
        @Schema(description = "권한 목록", example = "[]")
        private Collection<?> authorities;
        
        @Schema(description = "인증 상태", example = "true")
        private boolean authenticated;
    }

    @Schema(description = "토큰 정보 응답")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenInfoResponse {
        @Schema(description = "토큰 유효성", example = "true")
        private boolean valid;
        
        @Schema(description = "사용자 ID", example = "1")
        private String userId;
        
        @Schema(description = "토큰 만료 여부", example = "false")
        private boolean expired;
        
        @Schema(description = "응답 메시지", example = "토큰 정보를 성공적으로 조회했습니다.")
        private String message;
    }

    @Schema(description = "토큰 검증 요청")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidateTokenRequest {
        @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String token;
    }

    @Schema(description = "토큰 검증 응답")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor  
    public static class ValidateTokenResponse {
        @Schema(description = "토큰 유효성", example = "true")
        private boolean valid;
        
        @Schema(description = "토큰 만료 여부", example = "false")
        private boolean expired;
        
        @Schema(description = "사용자 ID", example = "1")
        private String userId;
        
        @Schema(description = "에러 메시지 (오류 시)")
        private String error;
    }
}