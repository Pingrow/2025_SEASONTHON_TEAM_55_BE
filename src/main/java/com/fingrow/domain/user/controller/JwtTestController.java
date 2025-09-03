package com.fingrow.domain.user.controller;

import com.fingrow.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class JwtTestController {

    private final JwtUtil jwtUtil;

    @PostMapping("/generate-token")
    public ResponseEntity<?> generateTestToken(@RequestBody(required = false) Map<String, String> request) {
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

    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint(Authentication authentication) {
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

    @GetMapping("/token-info")
    public ResponseEntity<?> getTokenInfo(@RequestHeader("Authorization") String authHeader) {
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

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
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