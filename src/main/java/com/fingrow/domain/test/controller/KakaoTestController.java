package com.fingrow.domain.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Tag(name = "카카오 테스트 API", description = "카카오 로그인 테스트를 위한 API")
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Slf4j
public class KakaoTestController {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;
    
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Operation(summary = "테스트용 설정 조회", description = "카카오 로그인 테스트에 필요한 설정 정보를 제공합니다.")
    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getTestConfig() {
        return ResponseEntity.ok(Map.of(
                "kakaoClientId", kakaoClientId,
                "kakaoRedirectUri", kakaoRedirectUri
        ));
    }

    @Operation(summary = "카카오 콜백 처리", description = "카카오 인증 코드를 받아 액세스 토큰으로 교환합니다.")
    @PostMapping("/kakao/callback")
    public ResponseEntity<Map<String, Object>> handleKakaoCallback(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "인증 코드가 필요합니다.",
                "success", false
            ));
        }

        try {
            log.info("카카오 토큰 요청 시작 - code: {}", code);
            
            // 카카오 토큰 요청
            String tokenUrl = "https://kauth.kakao.com/oauth/token";
            
            // HTTP 헤더 설정
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
            
            // 요청 파라미터 설정
            org.springframework.util.MultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoClientId);
            params.add("client_secret", kakaoClientSecret);
            params.add("redirect_uri", kakaoRedirectUri);
            params.add("code", code);
            
            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> tokenRequest = 
                new org.springframework.http.HttpEntity<>(params, headers);
            
            log.info("카카오 토큰 요청 파라미터: client_id={}, redirect_uri={}", kakaoClientId, kakaoRedirectUri);
            
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
            
            log.info("카카오 토큰 응답 상태: {}", tokenResponse.getStatusCode());

            Map<String, Object> tokenData = tokenResponse.getBody();
            
            if (tokenData != null && tokenData.get("access_token") != null) {
                // 사용자 정보 조회
                String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
                String accessToken = (String) tokenData.get("access_token");
                
                org.springframework.http.HttpHeaders userHeaders = new org.springframework.http.HttpHeaders();
                userHeaders.set("Authorization", "Bearer " + accessToken);
                org.springframework.http.HttpEntity<String> userEntity = new org.springframework.http.HttpEntity<>(userHeaders);
                
                ResponseEntity<Map> userResponse = restTemplate.exchange(
                    userInfoUrl, 
                    org.springframework.http.HttpMethod.GET, 
                    userEntity, 
                    Map.class
                );

                Map<String, Object> userInfo = userResponse.getBody();
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "카카오 로그인 성공",
                    "tokenInfo", tokenData,
                    "userInfo", userInfo != null ? userInfo : Map.of()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "토큰 교환에 실패했습니다.",
                    "success", false,
                    "details", tokenData != null ? tokenData : Map.of()
                ));
            }
            
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage(),
                "success", false
            ));
        }
    }

    @Operation(summary = "테스트 페이지", description = "카카오 로그인 테스트 페이지로 리다이렉트합니다.")
    @GetMapping("/kakao-login")
    public String kakaoLoginTest() {
        return "redirect:/test/kakao-login.html";
    }

}