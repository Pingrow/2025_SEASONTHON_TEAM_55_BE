package com.fingrow.domain.onboard.entity;

import com.fingrow.domain.user.entity.User;
import com.fingrow.global.enums.InvestmentGoal;
import com.fingrow.global.enums.InvestmentPeriod;
import com.fingrow.global.enums.PreferredInvestmentType;
import com.fingrow.global.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_goal", nullable = false)
    private InvestmentGoal investmentGoal;

    @Column(name = "target_amount", precision = 15, scale = 2)
    private BigDecimal targetAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_period", nullable = false)
    private InvestmentPeriod investmentPeriod;

    @ElementCollection(targetClass = PreferredInvestmentType.class, fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "user_preferred_investment_types",
            joinColumns = @JoinColumn(name = "investment_preference_id")
    )
    @Column(name = "investment_type")
    private Set<PreferredInvestmentType> preferredInvestmentTypes;

    @Column(name = "monthly_investment_amount", precision = 15, scale = 2)
    private BigDecimal monthlyInvestmentAmount;

    @Column(name = "current_investment_experience")
    private String currentInvestmentExperience;

    @Column(name = "additional_notes", length = 1000)
    private String additionalNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public InvestmentPreference(User user, RiskLevel riskLevel, InvestmentGoal investmentGoal, 
                               BigDecimal targetAmount, InvestmentPeriod investmentPeriod,
                               Set<PreferredInvestmentType> preferredInvestmentTypes,
                               BigDecimal monthlyInvestmentAmount, String currentInvestmentExperience,
                               String additionalNotes) {
        this.user = user;
        this.riskLevel = riskLevel;
        this.investmentGoal = investmentGoal;
        this.targetAmount = targetAmount;
        this.investmentPeriod = investmentPeriod;
        this.preferredInvestmentTypes = preferredInvestmentTypes;
        this.monthlyInvestmentAmount = monthlyInvestmentAmount;
        this.currentInvestmentExperience = currentInvestmentExperience;
        this.additionalNotes = additionalNotes;
    }

    public void updatePreferences(RiskLevel riskLevel, InvestmentGoal investmentGoal,
                                 BigDecimal targetAmount, InvestmentPeriod investmentPeriod,
                                 Set<PreferredInvestmentType> preferredInvestmentTypes,
                                 BigDecimal monthlyInvestmentAmount, String currentInvestmentExperience,
                                 String additionalNotes) {
        this.riskLevel = riskLevel;
        this.investmentGoal = investmentGoal;
        this.targetAmount = targetAmount;
        this.investmentPeriod = investmentPeriod;
        this.preferredInvestmentTypes = preferredInvestmentTypes;
        this.monthlyInvestmentAmount = monthlyInvestmentAmount;
        this.currentInvestmentExperience = currentInvestmentExperience;
        this.additionalNotes = additionalNotes;
    }

}