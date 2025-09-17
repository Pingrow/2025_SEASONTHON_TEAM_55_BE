package com.fingrow.domain.financial.etf.service;

import com.fingrow.domain.financial.etf.dto.EtfDto;
import com.fingrow.domain.financial.etf.entity.EtfProduct;
import com.fingrow.domain.financial.etf.repository.EtfProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.*;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EtfService {

    private final EtfProductRepository etfProductRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${krx.api.key}")
    private String krxApiKey;

    private static final String KRX_BASE_URL = "https://apis.data.go.kr/1160100/service/GetSecuritiesProductInfoService";

    // =========================== 동기화 ===========================
    public EtfDto.SyncResponse syncAllEtfData() {
        LocalDateTime startTime = LocalDateTime.now();
        int totalProcessed = 0, successCount = 0, failureCount = 0;
        List<String> failureReasons = new ArrayList<>();

        try {
            String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            URI uri = UriComponentsBuilder.fromHttpUrl(KRX_BASE_URL + "/getETFPriceInfo")
                    .queryParam("serviceKey", krxApiKey)
                    .queryParam("numOfRows", "1000")
                    .queryParam("resultType", "json")
                    .build()
                    .toUri();

            log.info("ETF API 호출: {}", uri.toString());

            String response = restTemplate.getForObject(uri, String.class);
            if (response == null || !response.trim().startsWith("{")) {
                throw new RuntimeException("API 응답이 JSON이 아님: " + response);
            }

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.path("response").path("body").path("items").path("item");

            if (itemsNode.isMissingNode() || itemsNode.isNull()) {
                failureReasons.add("ETF 데이터 없음");
                return createFailureResponse("ALL", totalProcessed, successCount, failureCount + 1, failureReasons, startTime);
            }

            if (!itemsNode.isArray()) {
                itemsNode = objectMapper.createArrayNode().add(itemsNode);
            }

            // 기존 해당 날짜 데이터 삭제
            List<EtfProduct> existingData = etfProductRepository.findByBasDt(baseDate);
            if (!existingData.isEmpty()) {
                etfProductRepository.deleteAll(existingData);
                log.info("기존 {} 날짜 데이터 {}건 삭제", baseDate, existingData.size());
            }

            // 새 데이터 저장
            for (JsonNode item : itemsNode) {
                totalProcessed++;
                try {
                    String srtnCd = getTextValue(item, "srtnCd");
                    if (srtnCd == null) {
                        failureReasons.add("srtnCd 누락");
                        failureCount++;
                        continue;
                    }

                    EtfProduct product = EtfProduct.builder()
                            // 상품 정보
                            .srtnCd(srtnCd)
                            .isinCd(getTextValue(item, "isinCd"))
                            .itmsNm(getTextValue(item, "itmsNm"))
                            .mrktCtg(getTextValue(item, "mrktCtg"))
                            .corpNm(extractCompanyFromName(getTextValue(item, "itmsNm")))
                            // 시세 정보
                            .basDt(baseDate)
                            .clpr(parseToLong(item.path("clpr")))
                            .vs(getTextValue(item, "vs"))
                            .fltRt(getTextValue(item, "fltRt"))
                            .mkp(parseToLong(item.path("mkp")))
                            .hipr(parseToLong(item.path("hipr")))
                            .lopr(parseToLong(item.path("lopr")))
                            .trqu(parseToLong(item.path("trqu")))
                            .trPrc(parseToLong(item.path("trPrc")))
                            .lstgStCnt(parseToLong(item.path("lstgStCnt")))
                            .mrktTotAmt(parseToLong(item.path("mrktTotAmt")))
                            .build();

                    etfProductRepository.save(product);
                    successCount++;

                } catch (Exception ex) {
                    failureCount++;
                    failureReasons.add("저장 실패: " + ex.getMessage());
                    log.error("저장 실패", ex);
                }
            }

            LocalDateTime endTime = LocalDateTime.now();
            return EtfDto.SyncResponse.builder()
                    .syncType("ALL")
                    .totalProcessed(totalProcessed)
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .failureReasons(failureReasons)
                    .startTime(startTime.toString())
                    .endTime(endTime.toString())
                    .duration(calculateDuration(startTime, endTime))
                    .build();

        } catch (Exception e) {
            log.error("ETF 동기화 실패", e);
            failureReasons.add("동기화 실패: " + e.getMessage());
            return createFailureResponse("ALL", totalProcessed, successCount, failureCount + 1, failureReasons, startTime);
        }
    }

    // =========================== 조회 ===========================
    public EtfDto.EtfListResponse getAllEtfProducts() {
        // 최신 날짜의 데이터 조회
        List<EtfProduct> etfList = etfProductRepository.findAllWithLatestDate();

        // 만약 최신 데이터가 없다면 오늘 데이터 조회
        if (etfList.isEmpty()) {
            String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            etfList = etfProductRepository.findByBasDt(todayDate);
        }

        List<EtfDto.EtfProductResponse> etfs = etfList.stream()
                .map(this::convertToDto)
                .toList();

        return EtfDto.EtfListResponse.builder()
                .etfs(etfs)
                .totalCount((long) etfs.size())
                .build();
    }

    // =========================== 유틸리티 메서드 ===========================

    private EtfDto.EtfProductResponse convertToDto(EtfProduct product) {
        return EtfDto.EtfProductResponse.builder()
                // 기본 정보
                .id(product.getId())
                .srtnCd(product.getSrtnCd())
                .isinCd(product.getIsinCd())
                .itmsNm(product.getItmsNm())
                .mrktCtg(product.getMrktCtg())
                .corpNm(product.getCorpNm())
                .createdAt(product.getCreatedAt())
                // 시세 정보
                .basDt(product.getBasDt())
                .clpr(product.getClpr())
                .vs(product.getVs())
                .fltRt(product.getFltRt())
                .mkp(product.getMkp())
                .hipr(product.getHipr())
                .lopr(product.getLopr())
                .trqu(product.getTrqu())
                .trPrc(product.getTrPrc())
                .lstgStCnt(product.getLstgStCnt())
                .mrktTotAmt(product.getMrktTotAmt())
                .build();
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode n = node.path(fieldName);
        if (n.isNull() || n.isMissingNode()) return null;
        String text = n.asText(null);
        return (text == null || text.trim().isEmpty()) ? null : text;
    }

    private Long parseToLong(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        String v = node.asText(null);
        if (v == null || v.trim().isEmpty()) return null;
        v = v.trim().replace(",", "").replace("+", "");
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractCompanyFromName(String etfName) {
        if (etfName == null || etfName.trim().isEmpty()) return "Unknown";
        String[] parts = etfName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : "Unknown";
    }

    private String calculateDuration(LocalDateTime start, LocalDateTime end) {
        Duration d = Duration.between(start, end);
        return String.format("%02d:%02d:%02d", d.getSeconds()/3600, (d.getSeconds()%3600)/60, d.getSeconds()%60);
    }

    private EtfDto.SyncResponse createFailureResponse(String syncType, int totalProcessed, int successCount,
                                                      int failureCount, List<String> failureReasons, LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        return EtfDto.SyncResponse.builder()
                .syncType(syncType)
                .totalProcessed(totalProcessed)
                .successCount(successCount)
                .failureCount(failureCount)
                .failureReasons(failureReasons)
                .startTime(startTime.toString())
                .endTime(endTime.toString())
                .duration(calculateDuration(startTime, endTime))
                .build();
    }

    // 간단한 리스트 조회 (FE용)
    public EtfDto.EtfSimpleListResponse getEtfSimpleList() {
        // 최신 날짜의 데이터 조회
        List<EtfProduct> etfList = etfProductRepository.findAllWithLatestDate();

        // 만약 최신 데이터가 없다면 오늘 데이터 조회
        String baseDate = null;
        if (etfList.isEmpty()) {
            baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            etfList = etfProductRepository.findByBasDt(baseDate);
        } else {
            baseDate = etfList.get(0).getBasDt(); // 첫 번째 항목의 기준일자
        }

        List<EtfDto.EtfListItemResponse> etfs = etfList.stream()
                .map(this::convertToSimpleDto)
                .toList();

        return EtfDto.EtfSimpleListResponse.builder()
                .etfs(etfs)
                .totalCount((long) etfs.size())
                .baseDate(baseDate)
                .build();
    }

    private EtfDto.EtfListItemResponse convertToSimpleDto(EtfProduct product) {
        return EtfDto.EtfListItemResponse.builder()
                .srtnCd(product.getSrtnCd())
                .itmsNm(product.getItmsNm())
                .corpNm(product.getCorpNm())
                .clpr(product.getClpr())
                .vs(product.getVs())
                .fltRt(product.getFltRt())
                .trqu(product.getTrqu())
                .mrktTotAmt(product.getMrktTotAmt())
                .build();
    }
}