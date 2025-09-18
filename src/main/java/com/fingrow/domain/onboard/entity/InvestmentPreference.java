package com.fingrow.domain.onboard.entity;

import com.fingrow.domain.user.entity.User;
import com.fingrow.global.enums.InvestmentMethod;
import com.fingrow.global.enums.LossTolerance;
import com.fingrow.global.enums.PreferredInvestmentType;
import com.fingrow.global.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "investment_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestmentPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "investment_goal", nullable = false, length = 100)
    private String investmentGoal;

    @Column(name = "target_amount", precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "investment_period")
    private Integer investmentPeriod;

    @ElementCollection(targetClass = PreferredInvestmentType.class, fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "user_preferred_investment_types",
            joinColumns = @JoinColumn(name = "investment_preference_id")
    )
    @Column(name = "investment_type")
    private Set<PreferredInvestmentType> preferredInvestmentTypes;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_method")
    private InvestmentMethod investmentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "loss_tolerance")
    private LossTolerance lossTolerance;

    @Column(name = "address", length = 500)
    private String address;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public InvestmentPreference(User user, RiskLevel riskLevel, String investmentGoal,
                               BigDecimal targetAmount, Integer investmentPeriod,
                               Set<PreferredInvestmentType> preferredInvestmentTypes,
                               InvestmentMethod investmentMethod, LossTolerance lossTolerance, String address) {
        this.user = user;
        this.riskLevel = riskLevel;
        this.investmentGoal = investmentGoal;
        this.targetAmount = targetAmount;
        this.investmentPeriod = investmentPeriod;
        this.preferredInvestmentTypes = preferredInvestmentTypes;
        this.investmentMethod = investmentMethod;
        this.lossTolerance = lossTolerance;
        this.address = address;
    }

    public void updatePreferences(RiskLevel riskLevel, String investmentGoal,
                                 BigDecimal targetAmount, Integer investmentPeriod,
                                 Set<PreferredInvestmentType> preferredInvestmentTypes,
                                 InvestmentMethod investmentMethod, LossTolerance lossTolerance, String address) {
        this.riskLevel = riskLevel;
        this.investmentGoal = investmentGoal;
        this.targetAmount = targetAmount;
        this.investmentPeriod = investmentPeriod;
        this.preferredInvestmentTypes = preferredInvestmentTypes;
        this.investmentMethod = investmentMethod;
        this.lossTolerance = lossTolerance;
        this.address = address;
    }

}