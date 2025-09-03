package com.fingrow.domain.user.service;

import com.fingrow.domain.user.entity.User;
import com.fingrow.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
// @Service  // OAuth2 의존성 제거로 임시 비활성화
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final UserRepository userRepository;
    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    public User processKakaoLogin(String authorizationCode) {
        // 1. 인가 코드로 액세스 토큰 획득
        String accessToken = getKakaoAccessToken(authorizationCode);
        
        // 2. 액세스 토큰으로 사용자 정보 조회
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);
        
        // 3. 사용자 정보로 회원가입 또는 로그인 처리
        return saveOrUpdateUser(userInfo);
    }

    private String getKakaoAccessToken(String authorizationCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", authorizationCode);
        
        try {
            Map<String, Object> response = webClient.post()
                    .uri("https://kauth.kakao.com/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            return (String) response.get("access_token");
        } catch (Exception e) {
            log.error("Failed to get Kakao access token", e);
            throw new RuntimeException("카카오 액세스 토큰 획득에 실패했습니다.");
        }
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to get Kakao user info", e);
            throw new RuntimeException("카카오 사용자 정보 조회에 실패했습니다.");
        }
    }

    private User saveOrUpdateUser(Map<String, Object> userInfo) {
        String providerId = userInfo.get("id").toString();
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        
        String email = kakaoAccount.get("email") != null ? (String) kakaoAccount.get("email") : null;
        String name = profile.get("nickname") != null ? (String) profile.get("nickname") : "Unknown";
        String profileImage = profile.get("profile_image_url") != null ? (String) profile.get("profile_image_url") : null;
        
        return userRepository.findByProviderAndProviderId(User.Provider.KAKAO, providerId)
                .map(existingUser -> {
                    existingUser.updateProfile(name, profileImage);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .profileImage(profileImage)
                        .provider(User.Provider.KAKAO)
                        .providerId(providerId)
                        .role(User.Role.USER)
                        .build()));
    }
}