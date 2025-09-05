package com.fingrow.domain.financial.service;

import com.fingrow.domain.financial.dto.*;
import com.fingrow.domain.financial.entity.DepositOption;
import com.fingrow.domain.financial.entity.DepositProduct;
import com.fingrow.domain.financial.entity.SavingOption;
import com.fingrow.domain.financial.entity.SavingProduct;
import com.fingrow.domain.financial.repository.DepositOptionRepository;
import com.fingrow.domain.financial.repository.DepositProductRepository;
import com.fingrow.domain.financial.repository.SavingOptionRepository;
import com.fingrow.domain.financial.repository.SavingProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class FinancialProductService {

    private final DepositProductRepository depositProductRepository;
    private final DepositOptionRepository depositOptionRepository;
    private final SavingProductRepository savingProductRepository;
    private final SavingOptionRepository savingOptionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${finlife.api.key:YOUR_API_KEY}")
    private String apiKey;

    private static final String BASE_URL = "https://finlife.fss.or.kr/finlifeapi";

    // =========================== 데이터 동기화 ===========================

    /**
     * 예금 상품 데이터 동기화
     */
    public void syncDepositProducts() {
        try {
            String url = BASE_URL + "/depositProductsSearch.json?auth=" + apiKey + "&topFinGrpNo=020000&pageNo=1";
            log.info("예금 상품 API 호출: {}", url);

            DepositApiResponse response = restTemplate.getForObject(url, DepositApiResponse.class);

            if (response == null || response.getResult() == null) {
                throw new RuntimeException("API 응답이 null입니다.");
            }

            // 기존 데이터 삭제
            depositOptionRepository.deleteAll();
            depositOptionRepository.flush();
            depositProductRepository.deleteAll();
            depositProductRepository.flush();
            log.info("기존 예금 데이터 삭제 완료");

            // 상품 정보 저장
            Map<String, DepositProduct> productMap = new HashMap<>();

            if (response.getResult().getBaseList() != null) {
                for (DepositProductDto dto : response.getResult().getBaseList()) {
                    DepositProduct product = DepositProduct.builder()
                            .finPrdtCd(dto.getFinPrdtCd())
                            .korCoNm(dto.getKorCoNm())
                            .finPrdtNm(dto.getFinPrdtNm())
                            .joinWay(dto.getJoinWay())
                            .mtrtInt(dto.getMtrtInt())
                            .spclCnd(dto.getSpclCnd())
                            .joinDeny(dto.getJoinDeny())
                            .joinMember(dto.getJoinMember())
                            .etcNote(dto.getEtcNote())
                            .maxLimit(dto.getMaxLimit())
                            .dclsMonth(dto.getDclsMonth())
                            .dclsStrtDay(dto.getDclsStrtDay())
                            .dclsEndDay(dto.getDclsEndDay())
                            .finCoNo(dto.getFinCoNo())
                            .build();

                    DepositProduct savedProduct = depositProductRepository.save(product);
                    productMap.put(dto.getFinPrdtCd(), savedProduct);
                }
            }

            // 옵션 정보 저장
            if (response.getResult().getOptionList() != null) {
                for (DepositOptionDto dto : response.getResult().getOptionList()) {
                    DepositProduct product = productMap.get(dto.getFinPrdtCd());
                    if (product != null) {
                        DepositOption option = DepositOption.builder()
                                .depositProduct(product)
                                .intrRateType(dto.getIntrRateType())
                                .intrRateTypeNm(dto.getIntrRateTypeNm())
                                .intrRate(dto.getIntrRate())
                                .intrRate2(dto.getIntrRate2())
                                .saveTrm(dto.getSaveTrm())
                                .build();

                        depositOptionRepository.save(option);
                    }
                }
            }

            log.info("예금 상품 데이터 동기화 완료: {} 개 상품", productMap.size());

        } catch (Exception e) {
            log.error("예금 상품 데이터 동기화 실패", e);
            throw new RuntimeException("예금 상품 데이터 동기화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 적금 상품 데이터 동기화
     */
    public void syncSavingProducts() {
        try {
            String url = BASE_URL + "/savingProductsSearch.json?auth=" + apiKey + "&topFinGrpNo=020000&pageNo=1";
            log.info("적금 상품 API 호출: {}", url);

            SavingApiResponse response = restTemplate.getForObject(url, SavingApiResponse.class);

            if (response == null || response.getResult() == null) {
                throw new RuntimeException("API 응답이 null입니다.");
            }

            // 기존 데이터 삭제
            savingOptionRepository.deleteAll();
            savingOptionRepository.flush();
            savingProductRepository.deleteAll();
            savingProductRepository.flush();
            log.info("기존 적금 데이터 삭제 완료");

            // 상품 정보 저장
            Map<String, SavingProduct> productMap = new HashMap<>();

            if (response.getResult().getBaseList() != null) {
                for (SavingProductDto dto : response.getResult().getBaseList()) {
                    SavingProduct product = SavingProduct.builder()
                            .finPrdtCd(dto.getFinPrdtCd())
                            .korCoNm(dto.getKorCoNm())
                            .finPrdtNm(dto.getFinPrdtNm())
                            .joinWay(dto.getJoinWay())
                            .mtrtInt(dto.getMtrtInt())
                            .spclCnd(dto.getSpclCnd())
                            .joinDeny(dto.getJoinDeny())
                            .joinMember(dto.getJoinMember())
                            .etcNote(dto.getEtcNote())
                            .maxLimit(dto.getMaxLimit())
                            .dclsMonth(dto.getDclsMonth())
                            .dclsStrtDay(dto.getDclsStrtDay())
                            .dclsEndDay(dto.getDclsEndDay())
                            .finCoNo(dto.getFinCoNo())
                            .build();

                    SavingProduct savedProduct = savingProductRepository.save(product);
                    productMap.put(dto.getFinPrdtCd(), savedProduct);
                }
            }

            // 옵션 정보 저장
            if (response.getResult().getOptionList() != null) {
                for (SavingOptionDto dto : response.getResult().getOptionList()) {
                    SavingProduct product = productMap.get(dto.getFinPrdtCd());
                    if (product != null) {
                        SavingOption option = SavingOption.builder()
                                .savingProduct(product)
                                .intrRateType(dto.getIntrRateType())
                                .intrRateTypeNm(dto.getIntrRateTypeNm())
                                .rsrvType(dto.getRsrvType())
                                .rsrvTypeNm(dto.getRsrvTypeNm())
                                .intrRate(dto.getIntrRate())
                                .intrRate2(dto.getIntrRate2())
                                .saveTrm(dto.getSaveTrm())
                                .build();

                        savingOptionRepository.save(option);
                    }
                }
            }

            log.info("적금 상품 데이터 동기화 완료: {} 개 상품", productMap.size());

        } catch (Exception e) {
            log.error("적금 상품 데이터 동기화 실패", e);
            throw new RuntimeException("적금 상품 데이터 동기화 실패: " + e.getMessage(), e);
        }
    }

    // =========================== 상품 추천 ===========================

    /**
     * 목표 기반 상품 추천
     */
    public RecommendationResponse recommendProducts(RecommendationRequest request) {
        log.info("상품 추천 요청: 목표금액={}, 목표기간={}개월, 현재보유={}",
                request.getTargetAmount(), request.getTargetMonths(), request.getCurrentAmount());

        List<ProductRecommendation> recommendations = new ArrayList<>();

        // 예금 상품 추천 (목표금액을 한번에 예치)
        List<DepositProduct> topDeposits = getTopDepositProducts(request.getTargetMonths(), 3);
        for (DepositProduct deposit : topDeposits) {
            ProductRecommendation rec = createDepositRecommendation(deposit, request);
            if (rec != null) {
                recommendations.add(rec);
            }
        }

        // 적금 상품 추천 (월납입으로 목표금액 달성)
        List<SavingProduct> topSavings = getTopSavingProducts(request.getTargetMonths(), 3);
        for (SavingProduct saving : topSavings) {
            ProductRecommendation rec = createSavingRecommendation(saving, request);
            if (rec != null) {
                recommendations.add(rec);
            }
        }

        // 최적 조합 계산
        OptimalCombination combination = calculateOptimalCombination(request);

        return RecommendationResponse.builder()
                .targetAmount(request.getTargetAmount())
                .targetMonths(request.getTargetMonths())
                .recommendations(recommendations)
                .optimalCombination(combination)
                .totalProducts(recommendations.size())
                .build();
    }

    /**
     * 최고 예금 상품 조회
     */
    private List<DepositProduct> getTopDepositProducts(Integer term, int limit) {
        List<DepositProduct> products = depositProductRepository.findByTermOrderByBestRateDesc(term);
        return products.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 최고 적금 상품 조회
     */
    private List<SavingProduct> getTopSavingProducts(Integer term, int limit) {
        List<SavingProduct> products = savingProductRepository.findByTermOrderByBestRateDesc(term);
        return products.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 예금 상품 추천 생성 - 간소화된 버전
     */
    private ProductRecommendation createDepositRecommendation(DepositProduct deposit, RecommendationRequest request) {
        DepositOption bestOption = deposit.getOptions().stream()
                .filter(o -> o.getSaveTrm().equals(request.getTargetMonths()))
                .max(Comparator.comparing(DepositOption::getBestRate))
                .orElse(null);

        if (bestOption == null) {
            return null;
        }

        double expectedAmount = calculateDepositReturn(
                (double) request.getTargetAmount(),
                bestOption.getBestRate(),
                request.getTargetMonths()
        );

        double expectedReturn = expectedAmount - (double) request.getTargetAmount();

        return ProductRecommendation.builder()
                .productType("예금")
                .bankName(deposit.getKorCoNm())
                .productName(deposit.getFinPrdtNm())
                .interestRate(bestOption.getBestRate())
                .term(bestOption.getSaveTrm())
                .expectedReturn(expectedReturn)
                .inputAmount(request.getTargetAmount())
                .maturityAmount((long) expectedAmount)
                .monthlyAmount(null) // 예금은 null
                .build();
    }

    /**
     * 적금 상품 추천 생성 - 간소화된 버전
     */
    private ProductRecommendation createSavingRecommendation(SavingProduct saving, RecommendationRequest request) {
        // 현재 보유 금액 고려한 부족 금액 계산
        long remainingAmount = request.getTargetAmount() - (request.getCurrentAmount() != null ? request.getCurrentAmount() : 0L);

        SavingOption bestOption = saving.getOptions().stream()
                .filter(o -> o.getSaveTrm().equals(request.getTargetMonths()))
                .max(Comparator.comparing(SavingOption::getBestRate))
                .orElse(null);

        if (bestOption == null) {
            return null;
        }

        double monthlyAmount = (double) remainingAmount / request.getTargetMonths();
        double expectedAmount = bestOption.calculateExpectedReturn(monthlyAmount, request.getTargetMonths());
        double expectedReturn = expectedAmount - (double) remainingAmount;

        return ProductRecommendation.builder()
                .productType("적금")
                .bankName(saving.getKorCoNm())
                .productName(saving.getFinPrdtNm())
                .interestRate(bestOption.getBestRate())
                .term(bestOption.getSaveTrm())
                .expectedReturn(expectedReturn)
                .inputAmount(null) // 적금은 null
                .maturityAmount((long) expectedAmount)
                .monthlyAmount((long) monthlyAmount)
                .build();
    }

    /**
     * 예금 수익률 계산 (단리)
     */
    private double calculateDepositReturn(double principal, Double rate, Integer months) {
        if (rate == null || rate <= 0) return principal;
        return principal * (1 + (rate / 100.0) * (months / 12.0));
    }

    /**
     * 최적 조합 계산
     */
    private OptimalCombination calculateOptimalCombination(RecommendationRequest request) {
        long currentAmount = request.getCurrentAmount() != null ? request.getCurrentAmount() : 0L;
        long remainingAmount = request.getTargetAmount() - currentAmount;
        long depositAmount = Math.min(currentAmount, request.getTargetAmount() / 2); // 현재 보유액은 예금으로
        long savingTotalAmount = remainingAmount; // 부족분은 적금으로
        double savingMonthlyAmount = (double) savingTotalAmount / request.getTargetMonths();

        List<CombinationProduct> products = new ArrayList<>();
        double totalExpectedReturn = 0.0;

        // 예금 상품 추가
        List<DepositProduct> topDeposits = getTopDepositProducts(request.getTargetMonths(), 1);
        if (!topDeposits.isEmpty()) {
            DepositProduct deposit = topDeposits.get(0);
            DepositOption bestOption = deposit.getOptions().stream()
                    .filter(o -> o.getSaveTrm().equals(request.getTargetMonths()))
                    .max(Comparator.comparing(DepositOption::getBestRate))
                    .orElse(null);

            if (bestOption != null) {
                double maturityAmount = calculateDepositReturn(
                        (double) depositAmount, bestOption.getBestRate(), request.getTargetMonths()
                );
                double expectedReturn = maturityAmount - depositAmount;
                totalExpectedReturn += expectedReturn;

                products.add(CombinationProduct.builder()
                        .productType("예금")
                        .bankName(deposit.getKorCoNm())
                        .productName(deposit.getFinPrdtNm())
                        .term(bestOption.getSaveTrm())
                        .interestRate(bestOption.getBestRate())
                        .specialCondition(deposit.getSpclCnd())
                        // 예금 전용 필드
                        .depositAmount(depositAmount)
                        .maturityAmount((long) maturityAmount)
                        // 적금 필드는 null
                        .monthlyAmount(null)
                        .totalSavingAmount(null)
                        .savingMaturityAmount(null)
                        // 공통 필드
                        .expectedReturn(expectedReturn)
                        .build());
            }
        }

        // 적금 상품 추가
        List<SavingProduct> topSavings = getTopSavingProducts(request.getTargetMonths(), 1);
        if (!topSavings.isEmpty()) {
            SavingProduct saving = topSavings.get(0);
            SavingOption bestOption = saving.getOptions().stream()
                    .filter(o -> o.getSaveTrm().equals(request.getTargetMonths()))
                    .max(Comparator.comparing(SavingOption::getBestRate))
                    .orElse(null);

            if (bestOption != null) {
                double savingMaturityAmount = bestOption.calculateExpectedReturn(
                        savingMonthlyAmount, request.getTargetMonths()
                );
                double expectedReturn = savingMaturityAmount - savingTotalAmount;
                totalExpectedReturn += expectedReturn;

                products.add(CombinationProduct.builder()
                        .productType("적금")
                        .bankName(saving.getKorCoNm())
                        .productName(saving.getFinPrdtNm())
                        .term(bestOption.getSaveTrm())
                        .interestRate(bestOption.getBestRate())
                        .specialCondition(saving.getSpclCnd())
                        // 예금 필드는 null
                        .depositAmount(null)
                        .maturityAmount(null)
                        // 적금 전용 필드
                        .monthlyAmount((long) savingMonthlyAmount)
                        .totalSavingAmount(savingTotalAmount)
                        .savingMaturityAmount((long) savingMaturityAmount)
                        // 공통 필드
                        .expectedReturn(expectedReturn)
                        .build());
            }
        }

        // 조합 요약 생성
        String combinationSummary = String.format("월 %.0f만원 · %d개월 · 예금+적금 혼합",
                savingMonthlyAmount / 10000.0, request.getTargetMonths());

        return OptimalCombination.builder()
                .products(products)
                .combinationSummary(combinationSummary)
                .totalExpectedReturn(totalExpectedReturn)
                .expectedTotalAmount((double) request.getTargetAmount() + totalExpectedReturn)
                .riskLevel("낮음")
                .description("최고 금리 예금과 적금의 최적 조합")
                .build();
    }

    // =========================== 조회 서비스 ===========================

    /**
     * 예금 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DepositProduct> getAllDepositProducts() {
        return depositProductRepository.findAll();
    }

    /**
     * 적금 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public List<SavingProduct> getAllSavingProducts() {
        return savingProductRepository.findAll();
    }

    /**
     * 상품 검색
     */
    @Transactional(readOnly = true)
    public SearchResponse searchProducts(String keyword) {
        List<DepositProduct> deposits = depositProductRepository.findByFinPrdtNmContaining(keyword);
        deposits.addAll(depositProductRepository.findByKorCoNmContaining(keyword));

        List<SavingProduct> savings = savingProductRepository.findByFinPrdtNmContaining(keyword);
        savings.addAll(savingProductRepository.findByKorCoNmContaining(keyword));

        List<ProductSummaryDto> allProducts = new ArrayList<>();

        // 예금 변환
        deposits.stream().distinct().forEach(d -> allProducts.add(convertDepositToSummary(d)));

        // 적금 변환
        savings.stream().distinct().forEach(s -> allProducts.add(convertSavingToSummary(s)));

        return SearchResponse.builder()
                .keyword(keyword)
                .products(allProducts)
                .totalCount(allProducts.size())
                .build();
    }

    // 변환 메서드들 추가
    private ProductSummaryDto convertDepositToSummary(DepositProduct deposit) {
        DepositOption bestOption = deposit.getOptions().stream()
                .max(Comparator.comparing(DepositOption::getBestRate))
                .orElse(null);

        return ProductSummaryDto.builder()
                .id(deposit.getId())
                .bankName(deposit.getKorCoNm())
                .productName(deposit.getFinPrdtNm())
                .productType("예금")
                .bestRate(bestOption != null ? bestOption.getBestRate() : 0.0)
                .bestTerm(bestOption != null ? bestOption.getSaveTrm() : null)
                .build();
    }

    private ProductSummaryDto convertSavingToSummary(SavingProduct saving) {
        SavingOption bestOption = saving.getOptions().stream()
                .max(Comparator.comparing(SavingOption::getBestRate))
                .orElse(null);

        return ProductSummaryDto.builder()
                .id(saving.getId())
                .bankName(saving.getKorCoNm())
                .productName(saving.getFinPrdtNm())
                .productType("적금")
                .bestRate(bestOption != null ? bestOption.getBestRate() : 0.0)
                .bestTerm(bestOption != null ? bestOption.getSaveTrm() : null)
                .build();
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    // 전체 조회용 메서드들도 추가
    @Transactional(readOnly = true)
    public List<ProductSummaryDto> getAllDepositProductsSummary() {
        return depositProductRepository.findAll().stream()
                .map(this::convertDepositToSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryDto> getAllSavingProductsSummary() {
        return savingProductRepository.findAll().stream()
                .map(this::convertSavingToSummary)
                .collect(Collectors.toList());
    }
}