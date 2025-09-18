package com.fingrow.domain.portfolio.controller;

import com.fingrow.domain.onboard.dto.OnboardDto;
import com.fingrow.domain.portfolio.dto.PortfolioDto;
import com.fingrow.domain.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    @Value("${flask.api.url:http://127.0.0.1:5001}")
    private String flaskApiUrl;

    private final RestTemplate restTemplate;
    private final PortfolioService portfolioService;

    @PostMapping("/recommend/percent")
    public ResponseEntity<PortfolioDto.RecommendResponse> getPortfolioAllocation(
            Authentication authentication,
            @RequestBody OnboardDto.OnboardRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String userId = (String) authentication.getPrincipal();
        PortfolioDto.RecommendResponse response = portfolioService.updatePortfolio(userId, request, true);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/recommend/products")
    public ResponseEntity<PortfolioDto.RecommendResponse> getPortfolioProducts(
            Authentication authentication,
            @RequestBody OnboardDto.OnboardRequest request) {

        if (authentication == null ) {
            return ResponseEntity.status(401).build();
        }

        String userId = (String) authentication.getPrincipal();
        PortfolioDto.RecommendResponse response = portfolioService.updatePortfolio(userId, request, false);

        return ResponseEntity.ok(response);
    }

}
