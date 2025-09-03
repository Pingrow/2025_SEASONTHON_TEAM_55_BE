package com.fingrow.domain.user.controller;

import com.fingrow.domain.user.entity.User;
import com.fingrow.domain.user.repository.UserRepository;
import com.fingrow.domain.user.service.KakaoOAuthService;
import com.fingrow.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final KakaoOAuthService kakaoOAuthService;

    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> request) {
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

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
        
        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String userId = (String) authentication.getPrincipal();
        
        return userRepository.findById(Long.parseLong(userId))
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("name", user.getName());
                    userInfo.put("profileImage", user.getProfileImage());
                    userInfo.put("provider", user.getProvider());
                    userInfo.put("role", user.getRole());
                    
                    return ResponseEntity.ok(userInfo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully logged out");
        
        return ResponseEntity.ok(response);
    }
}