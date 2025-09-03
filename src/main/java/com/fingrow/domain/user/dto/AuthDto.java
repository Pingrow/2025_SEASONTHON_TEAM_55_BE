package com.fingrow.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDto {

    @Schema(description = "토큰 갱신 요청")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String refreshToken;
    }

    @Schema(description = "토큰 갱신 응답")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenResponse {
        @Schema(description = "새로운 Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String accessToken;
    }

    @Schema(description = "사용자 정보 응답")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoResponse {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;
        
        @Schema(description = "이메일", example = "user@example.com")
        private String email;
        
        @Schema(description = "이름", example = "홍길동")
        private String name;
        
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        private String profileImage;
        
        @Schema(description = "OAuth 제공자", example = "KAKAO")
        private String provider;
        
        @Schema(description = "사용자 역할", example = "USER")
        private String role;
    }

    @Schema(description = "로그아웃 응답")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutResponse {
        @Schema(description = "응답 메시지", example = "Successfully logged out")
        private String message;
    }
}