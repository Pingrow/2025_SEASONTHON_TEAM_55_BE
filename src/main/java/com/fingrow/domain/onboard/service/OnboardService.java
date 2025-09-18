package com.fingrow.domain.onboard.service;

import com.fingrow.domain.onboard.dto.OnboardDto;
import com.fingrow.domain.onboard.entity.InvestmentPreference;
import com.fingrow.domain.onboard.repository.InvestmentPreferenceRepository;
import com.fingrow.domain.user.entity.User;
import com.fingrow.domain.user.repository.UserRepository;
import com.fingrow.global.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OnboardService {

    private final UserRepository userRepository;
    private final InvestmentPreferenceRepository investmentPreferenceRepository;
    
    public Optional<InvestmentPreference> getUserInvestmentPreference(String userId) {
        Long userIdLong;
        try {
            userIdLong = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효하지 않은 사용자 ID 형식입니다.");
        }
        
        User user = userRepository.findById(userIdLong)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return investmentPreferenceRepository.findByUser(user);
    }

    public OnboardDto.SurveyResponse processSurvey(String userId, OnboardDto.SurveyRequest request) {
        log.info("Processing onboard survey for user: {}", userId);

        Long userIdLong;
        try {
            userIdLong = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효하지 않은 사용자 ID 형식입니다.");
        }
        
        User user = userRepository.findById(userIdLong)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        InvestmentPreference preference = getOrCreateInvestmentPreference(user, request);
        OnboardDto.InvestmentAnalysis analysis = analyzeInvestmentProfile(request);
        RiskLevel riskLevel = determineRiskLevel(request.getLossTolerance(), request.getInvestmentPeriod(), request.getPreferredInvestmentTypes(), request.getInvestmentMethod());
        List<OnboardDto.RecommendedProduct> products = createRecommendedProducts(riskLevel);

        return OnboardDto.SurveyResponse.builder()
                .analysis(analysis)
                .recommendedProducts(products)
                .message("투자 성향 분석이 완료되었습니다.")
                .build();
    }

    // 이후에 수정하게 되면 Portfolio에서 수정 및 저장
    private InvestmentPreference getOrCreateInvestmentPreference(User user, OnboardDto.SurveyRequest request) {
        RiskLevel riskLevel = determineRiskLevel(request.getLossTolerance(), request.getInvestmentPeriod(), request.getPreferredInvestmentTypes(), request.getInvestmentMethod());

        InvestmentPreference preference = InvestmentPreference.builder()
                .user(user)
                .riskLevel(riskLevel)
                .investmentGoal(request.getInvestmentGoal())
                .targetAmount(request.getTargetAmount())
                .investmentPeriod(request.getInvestmentPeriod())
                .preferredInvestmentTypes(request.getPreferredInvestmentTypes())
                .investmentMethod(request.getInvestmentMethod())
                .lossTolerance(request.getLossTolerance())
                .address(request.getAddress())
                .build();

        return investmentPreferenceRepository.save(preference);
    }

    private OnboardDto.InvestmentAnalysis analyzeInvestmentProfile(OnboardDto.SurveyRequest request) {
        // 손실 감내도를 기준으로 투자 성향을 결정
        RiskLevel riskLevel = determineRiskLevel(request.getLossTolerance(), request.getInvestmentPeriod(), request.getPreferredInvestmentTypes(), request.getInvestmentMethod());
        
        String riskProfile = getRiskProfile(riskLevel);
        String investmentStrategy = getInvestmentStrategy(riskLevel);
        String expectedReturn = getExpectedReturn(riskLevel);
        String recommendation = getRecommendation(riskLevel);

        return OnboardDto.InvestmentAnalysis.builder()
                .riskProfile(riskProfile)
                .investmentStrategy(investmentStrategy)
                .expectedReturn(expectedReturn)
                .recommendation(recommendation)
                .build();
    }

    private List<OnboardDto.RecommendedProduct> createRecommendedProducts(RiskLevel riskLevel) {
        List<OnboardDto.RecommendedProduct> products = new ArrayList<>();
        
        // 투자 성향에 따라 하나의 상품 종류만 추천
        switch (riskLevel) {
            case STABLE:
                // 예/적금 추천
                products.add(OnboardDto.RecommendedProduct.builder()
                        .productType("예/적금")
                        .productName("KB 안심 정기예금")
                        .bankName("KB국민은행")
                        .interestRate(new BigDecimal("3.5"))
                        .description("원금 100% 보장되는 정기예금")
                        .reason("안정형 투자자에게 가장 적합한 안전한 예금상품")
                        .build());
                break;
            case STABILITY_SEEKING:
                // 예/적금 추천 (높은 금리)
                products.add(OnboardDto.RecommendedProduct.builder()
                        .productType("예/적금")
                        .productName("신한 Dream 적금")
                        .bankName("신한은행")
                        .interestRate(new BigDecimal("4.2"))
                        .description("우대 금리 적용 가능한 정기적금")
                        .reason("안정성을 우선하면서도 합리적인 수익을 추구하는 분께 적합")
                        .build());
                break;
            case RISK_NEUTRAL:
                // 국채 추천
                products.add(OnboardDto.RecommendedProduct.builder()
                        .productType("국채")
                        .productName("3년 만기 국고채")
                        .bankName("한국은행")
                        .interestRate(new BigDecimal("3.8"))
                        .description("국가 신용도를 바탕으로 한 안전한 채권")
                        .reason("중간 정도의 위험을 감내하며 안정적인 수익을 원하는 분께 적합")
                        .build());
                break;
            case ACTIVE_INVESTMENT:
                // ETF 추천
                products.add(OnboardDto.RecommendedProduct.builder()
                        .productType("ETF")
                        .productName("KODEX 200")
                        .bankName("삼성자산운용")
                        .interestRate(new BigDecimal("7.2"))
                        .description("코스피 200 지수를 추종하는 대표 ETF")
                        .reason("적극적인 투자를 통해 주식시장 수익률을 추구하는 분께 적합")
                        .build());
                break;
            case AGGRESSIVE_INVESTMENT:
                // 펀드 추천
                products.add(OnboardDto.RecommendedProduct.builder()
                        .productType("펀드")
                        .productName("미래에셋 글로벌 성장 펀드")
                        .bankName("미래에셋자산운용")
                        .interestRate(new BigDecimal("12.5"))
                        .description("글로벌 성장주에 투자하는 적극적 운용 펀드")
                        .reason("높은 위험을 감수하고 최대 수익을 추구하는 공격적 투자자에게 적합")
                        .build());
                break;
            default:
                products.add(OnboardDto.RecommendedProduct.builder()
                        .productType("예/적금")
                        .productName("기본 정기예금")
                        .bankName("우리은행")
                        .interestRate(new BigDecimal("3.0"))
                        .description("기본적인 예금상품")
                        .reason("안전한 투자를 원하는 분께 추천")
                        .build());
        }
        
        return products;
    }

    public OnboardDto.SurveyQuestions getSurveyQuestions() {
        return OnboardDto.SurveyQuestions.builder()
                .investmentMethodOptions(getInvestmentMethodOptions())
                .lossToleranceOptions(getLossToleranceOptions())
                .investmentTypeOptions(getInvestmentTypeOptions())
                .goalOptions(getGoalOptions())
                .build();
    }


    private OnboardDto.GoalOption[] getGoalOptions() {
        return new OnboardDto.GoalOption[]{
                OnboardDto.GoalOption.builder()
                        .value("은퇴 준비")
                        .label("은퇴 준비")
                        .description("노후 자금 마련")
                        .build(),
                OnboardDto.GoalOption.builder()
                        .value("주택 마련")
                        .label("주택 마련")
                        .description("내 집 마련 자금")
                        .build(),
                OnboardDto.GoalOption.builder()
                        .value("교육비 준비")
                        .label("교육비 준비")
                        .description("자녀 교육비 마련")
                        .build(),
                OnboardDto.GoalOption.builder()
                        .value("비상 자금")
                        .label("비상 자금")
                        .description("응급상황 대비 자금")
                        .build(),
                OnboardDto.GoalOption.builder()
                        .value("자산 증대")
                        .label("자산 증대")
                        .description("보유 자산 확대")
                        .build(),
                OnboardDto.GoalOption.builder()
                        .value("여행 자금")
                        .label("여행 자금")
                        .description("여행 및 휴가 자금")
                        .build(),
                OnboardDto.GoalOption.builder()
                        .value("결혼 자금")
                        .label("결혼 자금")
                        .description("결혼 준비 자금")
                        .build(),
                OnboardDto.GoalOption.builder()
                        .value("자동차 구매")
                        .label("자동차 구매")
                        .description("차량 구매 자금")
                        .build(),
                OnboardDto.GoalOption.builder()
                        .value("창업")
                        .label("창업")
                        .description("사업 시작 자금")
                        .build(),
                OnboardDto.GoalOption.builder()
                        .value("기타")
                        .label("기타")
                        .description("기타 목적")
                        .build()
        };
    }


    private OnboardDto.InvestmentTypeOption[] getInvestmentTypeOptions() {
        return new OnboardDto.InvestmentTypeOption[]{
                OnboardDto.InvestmentTypeOption.builder()
                        .value(PreferredInvestmentType.DEPOSIT_SAVINGS)
                        .label("예/적금")
                        .description("안전한 예금과 적금")
                        .build(),
                OnboardDto.InvestmentTypeOption.builder()
                        .value(PreferredInvestmentType.ETF)
                        .label("ETF")
                        .description("상장지수펀드")
                        .build(),
                OnboardDto.InvestmentTypeOption.builder()
                        .value(PreferredInvestmentType.GOVERNMENT_BONDS)
                        .label("국채")
                        .description("국가가 발행하는 채권")
                        .build(),
                OnboardDto.InvestmentTypeOption.builder()
                        .value(PreferredInvestmentType.FUNDS)
                        .label("펀드")
                        .description("전문가가 운용하는 투자상품")
                        .build()
        };
    }

    private OnboardDto.InvestmentMethodOption[] getInvestmentMethodOptions() {
        return new OnboardDto.InvestmentMethodOption[]{
                OnboardDto.InvestmentMethodOption.builder()
                        .value(InvestmentMethod.ONE_TIME_ONE_PLACE)
                        .label("한번에 한곳")
                        .description("한 번에 한 곳에 투자")
                        .build(),
                OnboardDto.InvestmentMethodOption.builder()
                        .value(InvestmentMethod.ONE_TIME_MULTIPLE_PLACES)
                        .label("한번에 여러곳")
                        .description("한 번에 여러 곳에 분산 투자")
                        .build(),
                OnboardDto.InvestmentMethodOption.builder()
                        .value(InvestmentMethod.MULTIPLE_TIMES_ONE_PLACE)
                        .label("여러번에 한곳")
                        .description("여러 번에 걸쳐 한 곳에 투자")
                        .build(),
                OnboardDto.InvestmentMethodOption.builder()
                        .value(InvestmentMethod.MULTIPLE_TIMES_MULTIPLE_PLACES)
                        .label("여러번에 여러곳")
                        .description("여러 번에 걸쳐 여러 곳에 분산 투자")
                        .build()
        };
    }

    private OnboardDto.LossToleranceOption[] getLossToleranceOptions() {
        return new OnboardDto.LossToleranceOption[]{
                OnboardDto.LossToleranceOption.builder()
                        .value(LossTolerance.NONE)
                        .label("손실 감내 못함")
                        .description("원금 손실을 전혀 받아들일 수 없음")
                        .build(),
                OnboardDto.LossToleranceOption.builder()
                        .value(LossTolerance.TEN_PERCENT)
                        .label("10%")
                        .description("10% 정도의 손실까지 감내 가능")
                        .build(),
                OnboardDto.LossToleranceOption.builder()
                        .value(LossTolerance.TWENTY_TO_THIRTY_PERCENT)
                        .label("20~30%")
                        .description("20~30% 손실까지 감내 가능")
                        .build(),
                OnboardDto.LossToleranceOption.builder()
                        .value(LossTolerance.HALF_OR_MORE)
                        .label("절반이상")
                        .description("투자금의 절반 이상 손실도 감내 가능")
                        .build()
        };
    }

    private RiskLevel determineRiskLevel(LossTolerance lossTolerance, Integer investmentPeriod, Set<PreferredInvestmentType> investmentTypes, InvestmentMethod investmentMethod) {
        // Rule-based 투자 성향 분석: 3가지 요소를 종합적으로 고려
        // 1. 손실 감내도 2. 투자 방식 3. 선호 투자 유형

        int riskScore = calculateRiskScore(lossTolerance, investmentMethod, investmentTypes);

        // 점수 기반 투자 성향 결정
        if (riskScore <= 3) {
            return RiskLevel.STABLE;
        } else if (riskScore <= 5) {
            return RiskLevel.STABILITY_SEEKING;
        } else if (riskScore <= 7) {
            return RiskLevel.RISK_NEUTRAL;
        } else if (riskScore <= 9) {
            return RiskLevel.ACTIVE_INVESTMENT;
        } else {
            return RiskLevel.AGGRESSIVE_INVESTMENT;
        }
    }

    private int calculateRiskScore(LossTolerance lossTolerance, InvestmentMethod investmentMethod, Set<PreferredInvestmentType> investmentTypes) {
        int score = 0;

        // 1. 손실 감내도 점수 (0-4점)
        switch (lossTolerance) {
            case NONE:
                score += 0;
                break;
            case TEN_PERCENT:
                score += 2;
                break;
            case TWENTY_TO_THIRTY_PERCENT:
                score += 3;
                break;
            case HALF_OR_MORE:
                score += 4;
                break;
        }

        // 2. 투자 방식 점수 (0-3점)
        switch (investmentMethod) {
            case ONE_TIME_ONE_PLACE:
                score += 1; // 가장 보수적
                break;
            case ONE_TIME_MULTIPLE_PLACES:
                score += 2; // 분산투자 선호
                break;
            case MULTIPLE_TIMES_ONE_PLACE:
                score += 2; // 적립식 투자
                break;
            case MULTIPLE_TIMES_MULTIPLE_PLACES:
                score += 3; // 가장 적극적인 분산투자
                break;
        }

        // 3. 선호 투자 유형 점수 (0-4점)
        int typeScore = 0;
        if (investmentTypes.contains(PreferredInvestmentType.DEPOSIT_SAVINGS)) {
            typeScore += 0; // 가장 안전
        }
        if (investmentTypes.contains(PreferredInvestmentType.GOVERNMENT_BONDS)) {
            typeScore += 1; // 안전
        }
        if (investmentTypes.contains(PreferredInvestmentType.ETF)) {
            typeScore += 2; // 중간 위험
        }
        if (investmentTypes.contains(PreferredInvestmentType.FUNDS)) {
            typeScore += 3; // 높은 위험
        }

        // 복수 선택시 가중치 적용
        int selectedCount = investmentTypes.size();
        if (selectedCount > 1) {
            typeScore = (int) Math.ceil(typeScore * 0.8); // 분산 선호시 약간 보수적으로
        }

        score += Math.min(typeScore, 4); // 최대 4점으로 제한

        return Math.min(score, 11); // 최대 11점으로 제한
    }

    private String getRiskProfile(RiskLevel riskLevel) {
        switch (riskLevel) {
            case STABLE:
                return "안정형";
            case STABILITY_SEEKING:
                return "안정추구형";
            case RISK_NEUTRAL:
                return "위험중립형";
            case ACTIVE_INVESTMENT:
                return "적극투자형";
            case AGGRESSIVE_INVESTMENT:
                return "공격투자형";
            default:
                return "안정형";
        }
    }

    private String getInvestmentStrategy(RiskLevel riskLevel) {
        switch (riskLevel) {
            case STABLE:
                return "원금보장 중심의 안정적 투자";
            case STABILITY_SEEKING:
                return "안정성을 우선하되 적절한 수익 추구";
            case RISK_NEUTRAL:
                return "위험과 수익의 균형잡힌 투자";
            case ACTIVE_INVESTMENT:
                return "적극적인 수익 추구를 위한 투자";
            case AGGRESSIVE_INVESTMENT:
                return "고위험 고수익을 추구하는 공격적 투자";
            default:
                return "원금보장 중심의 안정적 투자";
        }
    }

    private String getExpectedReturn(RiskLevel riskLevel) {
        switch (riskLevel) {
            case STABLE:
                return "연 2-3% 수익률";
            case STABILITY_SEEKING:
                return "연 3-4% 수익률";
            case RISK_NEUTRAL:
                return "연 4-5% 수익률";
            case ACTIVE_INVESTMENT:
                return "연 5-7% 수익률";
            case AGGRESSIVE_INVESTMENT:
                return "연 7% 이상 수익률";
            default:
                return "연 2-3% 수익률";
        }
    }

    private String getRecommendation(RiskLevel riskLevel) {
        switch (riskLevel) {
            case STABLE:
                return "정기예금, 적금 등 원금보장 상품을 권장합니다";
            case STABILITY_SEEKING:
                return "정기예금과 안전한 채권형 펀드를 조합한 투자를 권장합니다";
            case RISK_NEUTRAL:
                return "예적금과 균형형 펀드를 적절히 배분한 투자를 권장합니다";
            case ACTIVE_INVESTMENT:
                return "성장형 펀드와 주식형 펀드를 포함한 적극적 투자를 권장합니다";
            case AGGRESSIVE_INVESTMENT:
                return "고수익 투자상품과 주식 직접투자를 포함한 공격적 투자를 권장합니다";
            default:
                return "정기예금, 적금 등 원금보장 상품을 권장합니다";
        }
    }
}