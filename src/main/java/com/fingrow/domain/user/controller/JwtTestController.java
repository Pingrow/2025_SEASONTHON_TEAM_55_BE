package com.fingrow.domain.user.controller;

import com.fingrow.domain.user.dto.JwtTestDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "JWT 테스트 API", description = "JWT 토큰 테스트용 API (개발용)")
@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class JwtTestController {

    private final JwtUtil jwtUtil;

    @Operation(summary = "테스트 JWT 토큰 생성", description = "개발/테스트용 JWT 토큰을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"userId\": \"1\", \"message\": \"테스트용 JWT 토큰이 생성되었습니다.\"}")))
    })
    @PostMapping("/generate-token")
    public ResponseEntity<?> generateTestToken(
            @Parameter(description = "사용자 ID (선택사항)", 
                    schema = @Schema(example = "{\"userId\": \"1\"}"))
            @RequestBody(required = false) Map<String, String> request) {
        // 테스트용 사용자 ID (실제로는 요청에서 받거나 기본값 사용)
        String userId = "1";
        if (request != null && request.containsKey("userId")) {
            userId = request.get("userId");
        }
        
        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("userId", userId);
        response.put("message", "테스트용 JWT 토큰이 생성되었습니다.");
        
        log.info("Generated test JWT token for userId: {}", userId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "인증 필수 엔드포인트 테스트", description = "JWT 인증이 필요한 엔드포인트의 동작을 테스트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"인증된 사용자만 접근 가능한 API입니다.\", \"userId\": \"1\", \"authorities\": [], \"authenticated\": true}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint(
            @Parameter(hidden = true) Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }
        
        String userId = (String) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "인증된 사용자만 접근 가능한 API입니다.");
        response.put("userId", userId);
        response.put("authorities", authentication.getAuthorities());
        response.put("authenticated", authentication.isAuthenticated());
        
        log.info("Protected endpoint accessed by userId: {}", userId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "JWT 토큰 정보 조회", description = "JWT 토큰의 유효성 및 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"valid\": true, \"userId\": \"1\", \"expired\": false, \"message\": \"토큰 정보를 성공적으로 조회했습니다.\"}"))),
            @ApiResponse(responseCode = "400", description = "Authorization 헤더 누락"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @GetMapping("/token-info")
    public ResponseEntity<?> getTokenInfo(
            @Parameter(description = "Authorization 헤더 (Bearer {token})", required = true)
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Authorization 헤더가 필요합니다.");
            }
            
            String token = authHeader.substring(7);
            
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
            }
            
            String userId = jwtUtil.getUserIdFromToken(token);
            boolean isExpired = jwtUtil.isTokenExpired(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("userId", userId);
            response.put("expired", isExpired);
            response.put("message", "토큰 정보를 성공적으로 조회했습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token info error", e);
            return ResponseEntity.status(401).body("토큰 정보 조회 실패: " + e.getMessage());
        }
    }

    @Operation(summary = "JWT 토큰 유효성 검증", description = "제공된 JWT 토큰의 유효성을 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 검증 완료 (성공/실패 모두 포함)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"valid\": true, \"expired\": false, \"userId\": \"1\"}"))),
            @ApiResponse(responseCode = "400", description = "토큰 누락")
    })
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(
            @Parameter(description = "JWT 토큰", required = true,
                    schema = @Schema(example = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}"))
            @RequestBody Map<String, String> request) {
        String token = request.get("token");
        
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("토큰이 필요합니다.");
        }
        
        try {
            boolean isValid = jwtUtil.validateToken(token);
            String userId = null;
            boolean isExpired = true;
            
            if (isValid) {
                userId = jwtUtil.getUserIdFromToken(token);
                isExpired = jwtUtil.isTokenExpired(token);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("expired", isExpired);
            if (userId != null) {
                response.put("userId", userId);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token validation error", e);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.ok(response);
        }
    }
}