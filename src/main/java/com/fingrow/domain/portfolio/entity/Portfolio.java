package com.fingrow.domain.portfolio.entity;

import com.fingrow.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Entity
@Table(name = "portfolio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String riskLevel;       // 안정형, 위험중립형 등
    private Long targetAmount;      // 목표 금액
    private Integer investmentPeriod; // 투자 기간(개월)
    private Double expectedTotal;   // 예상 총 수익

    // JSON 저장용 (allocation, products)
    @Lob
    private String allocationJson;

    @Lob
    private String recommendedProductsJson;

    private String gptReasoning;    // GPT 추천 근거
}

