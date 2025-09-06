package com.fingrow.domain.user.controller;

import com.fingrow.domain.user.dto.AuthDto;
import com.fingrow.domain.user.entity.User;
import com.fingrow.domain.user.repository.UserRepository;
import com.fingrow.domain.user.service.KakaoOAuthService;
import com.fingrow.global.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "인증 API", description = "사용자 인증 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final KakaoOAuthService kakaoOAuthService;

    @Operation(summary = "카카오 로그인", description = "카카오 인증 코드를 받아 JWT 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"user\": {\"id\": 1, \"email\": \"user@example.com\", \"name\": \"홍길동\"}}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 인증 코드 또는 로그인 실패")
    })
    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(
            @Parameter(description = "카카오 인증 코드", required = true,
                    schema = @Schema(example = "{\"code\": \"authorization_code_from_kakao\"}"))
            @RequestBody Map<String, String> request) {
        try {
            String authorizationCode = request.get("code");
            
            if (authorizationCode == null || authorizationCode.isEmpty()) {
                return ResponseEntity.badRequest().body("Authorization code is required");
            }
            
            // 카카오 OAuth 처리
            User user = kakaoOAuthService.processKakaoLogin(authorizationCode);
            
            // JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(user.getId().toString());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());
            
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "profileImage", user.getProfileImage(),
                    "provider", user.getProvider(),
                    "role", user.getRole()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Kakao login failed", e);
            return ResponseEntity.badRequest().body("로그인에 실패했습니다: " + e.getMessage());
        }
    }

}