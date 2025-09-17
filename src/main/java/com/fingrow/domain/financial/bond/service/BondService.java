package com.fingrow.domain.financial.bond.service;

import com.fingrow.domain.financial.bond.dto.*;
import com.fingrow.domain.financial.bond.entity.Bond;
import com.fingrow.domain.financial.bond.repository.BondRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class BondService {

    private final BondRepository bondRepository;
    private final RestTemplate restTemplate;

    @Value("${KRX_API_KEY}")
    private String apiKey;

    private static final String BASE_URL = "https://apis.data.go.kr/1160100/service/GetBondTradInfoService/getIssuIssuItemStat";

    public void syncBondData() {
        syncBondData("금융채");
    }

    public void syncBondData(String bondType) {
        try {
            log.info("채권 데이터 동기화 시작: {}", bondType);

            String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            String url = String.format("%s?serviceKey=%s&pageNo=1&numOfRows=1000&resultType=json&scrsItmsKcdNm=%s&basDt=%s",
                    BASE_URL, encodedApiKey, URLEncoder.encode(bondType, StandardCharsets.UTF_8), today);

            log.info("채권 API 호출: {}", url);

            BondApiResponse response = restTemplate.getForObject(url, BondApiResponse.class);

            if (response == null || response.getResponse() == null || response.getResponse().getBody() == null) {
                throw new RuntimeException("API 응답이 null입니다.");
            }

            if (!"00".equals(response.getResponse().getHeader().getResultCode())) {
                throw new RuntimeException("API 호출 실패: " + response.getResponse().getHeader().getResultMsg());
            }

            List<BondDto> bondDtos = response.getResponse().getBody().getItems().getItem();
            if (bondDtos == null || bondDtos.isEmpty()) {
                log.warn("API 응답에서 채권 데이터를 찾을 수 없습니다.");
                return;
            }

            // 해당 종류의 기존 데이터 삭제
            List<Bond> existingBonds = bondRepository.findByScrsItmsKcdNm(bondType);
            bondRepository.deleteAll(existingBonds);
            bondRepository.flush();
            log.info("기존 {} 데이터 {} 건 삭제 완료", bondType, existingBonds.size());

            // 새 데이터 저장
            List<Bond> bonds = new ArrayList<>();
            for (BondDto dto : bondDtos) {
                try {
                    Bond bond = convertDtoToEntity(dto);
                    if (bond != null) {
                        bonds.add(bond);
                    }
                } catch (Exception e) {
                    log.warn("채권 데이터 변환 실패: {} - {}", dto.getIsinCd(), e.getMessage());
                }
            }

            bondRepository.saveAll(bonds);
            log.info("{} 채권 데이터 동기화 완료: {} 건", bondType, bonds.size());

        } catch (Exception e) {
            log.error("채권 데이터 동기화 실패: {}", bondType, e);
            throw new RuntimeException("채권 데이터 동기화 실패: " + e.getMessage(), e);
        }
    }

    private Bond convertDtoToEntity(BondDto dto) {
        try {
            Double interestRate = null;
            if (dto.getBondSrfcInrt() != null && !dto.getBondSrfcInrt().trim().isEmpty()) {
                try {
                    interestRate = Double.parseDouble(dto.getBondSrfcInrt());
                } catch (NumberFormatException e) {
                    log.warn("금리 파싱 실패: {} - {}", dto.getIsinCd(), dto.getBondSrfcInrt());
                }
            }

            LocalDate maturityDate = null;
            if (dto.getBondExprDt() != null && !dto.getBondExprDt().trim().isEmpty()) {
                try {
                    maturityDate = LocalDate.parse(dto.getBondExprDt(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                } catch (DateTimeParseException e) {
                    log.warn("만기일 파싱 실패: {} - {}", dto.getIsinCd(), dto.getBondExprDt());
                }
            }

            return Bond.builder()
                    .isinCd(dto.getIsinCd())
                    .isinCdNm(dto.getIsinCdNm())
                    .bondIsurNm(dto.getBondIsurNm())
                    .bondSrfcInrt(interestRate)
                    .bondExprDt(maturityDate)
                    .scrsItmsKcdNm(dto.getScrsItmsKcdNm())
                    .basDt(dto.getBasDt())
                    .build();

        } catch (Exception e) {
            log.warn("DTO to Entity 변환 실패: {}", dto.getIsinCd(), e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public BondTopResponse getTopBonds() {
        return getTopBonds("금융채", 5);
    }

    @Transactional(readOnly = true)
    public BondTopResponse getTopBonds(String bondType, Integer topCount) {
        LocalDate today = LocalDate.now();
        
        List<Bond> topByRate = bondRepository.findFutureBondsByTypeOrderByInterestRateDesc(today, bondType)
                .stream()
                .limit(topCount)
                .collect(Collectors.toList());

        List<Bond> topByMaturity = bondRepository.findBondsByMaturityPeriodOrderByInterestRateDesc(today, today.plusYears(10))
                .stream()
                .filter(b -> bondType.equals(b.getScrsItmsKcdNm()))
                .sorted(Comparator.comparing(Bond::getBondExprDt))
                .limit(topCount)
                .collect(Collectors.toList());

        return BondTopResponse.builder()
                .topByInterestRate(topByRate.stream().map(this::convertToSummaryDto).collect(Collectors.toList()))
                .topByMaturity(topByMaturity.stream().map(this::convertToSummaryDto).collect(Collectors.toList()))
                .bondType(bondType)
                .topCount(topCount)
                .syncDate(LocalDate.now().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public BondSearchResponse searchBonds(String keyword) {
        List<Bond> bonds = new ArrayList<>();
        
        bonds.addAll(bondRepository.findByIsinCdNmContainingIgnoreCase(keyword));
        bonds.addAll(bondRepository.findByBondIsurNmContainingIgnoreCase(keyword));

        // 중복 제거
        List<Bond> uniqueBonds = bonds.stream()
                .distinct()
                .collect(Collectors.toList());

        return BondSearchResponse.builder()
                .keyword(keyword)
                .bonds(uniqueBonds.stream().map(this::convertToSummaryDto).collect(Collectors.toList()))
                .totalCount(uniqueBonds.size())
                .sortBy("금리순")
                .build();
    }

    @Transactional(readOnly = true)
    public List<BondSummaryDto> getAllBonds() {
        return bondRepository.findAll().stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BondSummaryDto> getFutureBondsByType(String bondType) {
        return bondRepository.findFutureBondsByTypeOrderByInterestRateDesc(LocalDate.now(), bondType)
                .stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BondSummaryDto> getBondsByMinRate(Double minRate) {
        return bondRepository.findFutureBondsByMinRateOrderByInterestRateDesc(LocalDate.now(), minRate)
                .stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());
    }

    private BondSummaryDto convertToSummaryDto(Bond bond) {
        return BondSummaryDto.builder()
                .id(bond.getId())
                .isinCd(bond.getIsinCd())
                .bondName(bond.getIsinCdNm())
                .issuerName(bond.getBondIsurNm())
                .interestRate(bond.getBondSrfcInrt())
                .maturityDate(bond.getBondExprDt())
                .bondType(bond.getScrsItmsKcdNm())
                .daysToMaturity(bond.getDaysToMaturity())
                .isMatured(bond.isMatured())
                .build();
    }
}