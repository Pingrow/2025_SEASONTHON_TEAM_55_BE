package com.fingrow.global.config;

import com.fingrow.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 활성화 - CorsConfig의 설정을 사용
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // 온보딩 API 경로 추가 - 설문 문항 조회는 public, 나머지는 인증 필요
                        .requestMatchers("/api/v1/onboard/questions").permitAll()
                        
                        // 테스트 API 경로 추가 - 테스트용 API는 public
                        .requestMatchers("/api/v1/test/**").permitAll()

                        // 카카오 콜백 경로 추가
                        .requestMatchers("/callback").permitAll()

                        // 정적 파일 접근 허용
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/*.html").permitAll()

                        // 금융상품 API 경로 추가 - 모든 HTTP 메서드 허용
                        .requestMatchers("/api/financial/**").permitAll()
                        .requestMatchers("/api/financial/sync/**").permitAll()

                        // Swagger 관련 경로 정리(하단)
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}