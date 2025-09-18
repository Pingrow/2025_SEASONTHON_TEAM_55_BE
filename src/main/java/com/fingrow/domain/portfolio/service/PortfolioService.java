package com.fingrow.domain.portfolio.service;

import com.fingrow.domain.onboard.dto.OnboardDto;
import com.fingrow.domain.onboard.entity.InvestmentPreference;
import com.fingrow.domain.onboard.repository.InvestmentPreferenceRepository;
import com.fingrow.domain.portfolio.dto.PortfolioDto;
import com.fingrow.domain.portfolio.entity.Portfolio;
import com.fingrow.domain.portfolio.repository.PortfolioRepository;
import com.fingrow.domain.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fingrow.domain.user.repository.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final InvestmentPreferenceRepository investmentPreferenceRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${flask.api.url:http://127.0.0.1:5001}")
    private String flaskApiUrl;

    @Transactional
    public PortfolioDto.RecommendResponse updatePortfolio(String userId, OnboardDto.OnboardRequest request, boolean percentMode) {
        // 1. userId 변환
        Long userIdLong;
        try {
            userIdLong = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효하지 않은 사용자 ID 형식입니다.");
        }

        User user = userRepository.findById(userIdLong)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        InvestmentPreference preference = investmentPreferenceRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("온보딩 정보가 없습니다."));

        // 2. Flask API 호출
        String endpoint = percentMode ? "/recommend/percent" : "/recommend/products";
        String url = flaskApiUrl + endpoint;

        ResponseEntity<PortfolioDto.RecommendResponse> response =
                restTemplate.postForEntity(url, request, PortfolioDto.RecommendResponse.class);

        PortfolioDto.RecommendResponse dto = response.getBody();
        if (dto == null) {
            throw new RuntimeException("추천 결과가 비어있습니다.");
        }

        // 3. DB 저장
        Portfolio portfolio = Portfolio.builder()
                .riskLevel(dto.getRiskLevel())
                .targetAmount(dto.getTargetAmount())
                .investmentPeriod(dto.getInvestmentPeriod())
                .expectedTotal(dto.getExpectedTotal())
                .allocationJson(writeJson(dto.getAllocation()))
                .recommendedProductsJson(writeJson(dto.getRecommendedProducts()))
                .gptReasoning(dto.getGptReasoning())
                .build();

        portfolioRepository.save(portfolio);

        return dto;
    }



    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }
}