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

    @Operation(summary = "카카오 로그인", description = "플러터에서 받은 카카오 액세스 토큰으로 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"user\": {\"id\": 1, \"email\": \"user@example.com\", \"name\": \"홍길동\", \"profileImage\": \"profile_url\", \"provider\": \"KAKAO\", \"role\": \"USER\"}}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 액세스 토큰 또는 로그인 실패")
    })
    @PostMapping(value = "/kakao", consumes = {"application/json", "application/x-www-form-urlencoded"})
    public ResponseEntity<?> kakaoLogin(
            @Parameter(description = "카카오 액세스 토큰", required = true,
                    schema = @Schema(example = "{\"accessToken\": \"kakao_access_token_from_flutter\"}"))
            @RequestBody AuthDto.KakaoLoginRequest request) {
        try {
            String kakaoAccessToken = request.getAccessToken();

            if (kakaoAccessToken == null || kakaoAccessToken.isEmpty()) {
                return ResponseEntity.badRequest().body("Kakao access token is required");
            }

            // 카카오 액세스 토큰으로 사용자 정보 조회 및 처리
            User user = kakaoOAuthService.processKakaoLoginWithToken(kakaoAccessToken);

            // 서버 JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(user.getId().toString());

            // 응답 생성
            AuthDto.LoginResponse response = AuthDto.LoginResponse.builder()
                    .accessToken(accessToken)
                    .user(AuthDto.UserInfo.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .name(user.getName())
                            .profileImage(user.getProfileImage())
                            .provider(user.getProvider().name())
                            .role(user.getRole().name())
                            .build())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Kakao login failed", e);
            return ResponseEntity.badRequest().body("로그인에 실패했습니다: " + e.getMessage());
        }
    }


}