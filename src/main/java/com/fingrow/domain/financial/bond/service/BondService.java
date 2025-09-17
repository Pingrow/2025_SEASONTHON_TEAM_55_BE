package com.fingrow.domain.financial.bond.service;

import com.fingrow.domain.financial.bond.dto.BondDto;
import com.fingrow.domain.financial.bond.dto.BondResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BondService {

    private final RestTemplate restTemplate;

    @Value("${bond.api.key}")
    private String bondApiKey;

    private static final String BOND_API_URL = "https://apis.data.go.kr/1160100/service/GetBondTradInfoService/getIssuIssuItemStat";

    public BondResponse getBondInfo() {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String url = UriComponentsBuilder.fromHttpUrl(BOND_API_URL)
                    .queryParam("serviceKey", bondApiKey)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 1000)
                    .queryParam("resultType", "json")
                    .queryParam("scrsItmsKcdNm", "금융채")
                    .queryParam("basDt", today)
                    .build()
                    .toUriString();

            log.info("채권 API 호출: {}", url);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                throw new RuntimeException("API 응답이 null입니다.");
            }

            List<Map<String, Object>> items = extractItems(response);
            List<BondDto> processedBonds = processApiResponse(items);

            List<BondDto> topByInterest = processedBonds.stream()
                    .sorted((a, b) -> Double.compare(
                            b.getBondSrfcInrt() != null ? b.getBondSrfcInrt() : 0.0,
                            a.getBondSrfcInrt() != null ? a.getBondSrfcInrt() : 0.0
                    ))
                    .limit(5)
                    .collect(Collectors.toList());

            List<BondDto> topByMaturity = processedBonds.stream()
                    .filter(bond -> bond.getBondExprDt() != null)
                    .sorted(Comparator.comparing(BondDto::getBondExprDt))
                    .limit(5)
                    .collect(Collectors.toList());

            BondResponse.BondData bondData = BondResponse.BondData.builder()
                    .sortByInterest(topByInterest)
                    .sortByMaturity(topByMaturity)
                    .build();

            return BondResponse.builder()
                    .success(true)
                    .message(String.format("%d개의 금융채 상품을 조회했습니다.", topByInterest.size() + topByMaturity.size()))
                    .data(bondData)
                    .build();

        } catch (Exception e) {
            log.error("채권 정보 조회 실패", e);
            throw new RuntimeException("채권 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItems(Map<String, Object> response) {
        try {
            Map<String, Object> responseBody = (Map<String, Object>) response.get("response");
            Map<String, Object> body = (Map<String, Object>) responseBody.get("body");
            Map<String, Object> items = (Map<String, Object>) body.get("items");
            Object item = items.get("item");

            if (item instanceof List) {
                return (List<Map<String, Object>>) item;
            } else if (item instanceof Map) {
                return Collections.singletonList((Map<String, Object>) item);
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("API 응답 파싱 실패", e);
            return Collections.emptyList();
        }
    }

    private List<BondDto> processApiResponse(List<Map<String, Object>> items) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return items.stream()
                .map(this::mapToBondDto)
                .filter(bond -> bond.getBondExprDt() != null && bond.getBondExprDt().compareTo(today) >= 0)
                .collect(Collectors.toList());
    }

    private BondDto mapToBondDto(Map<String, Object> item) {
        try {
            String bondExprDt = convertDateFormat((String) item.get("bondExprDt"));
            Double bondSrfcInrt = parseDouble(item.get("bondSrfcInrt"));

            return BondDto.builder()
                    .bondIsurNm((String) item.get("bondIsurNm"))
                    .isinCdNm((String) item.get("isinCdNm"))
                    .bondSrfcInrt(bondSrfcInrt)
                    .bondExprDt(bondExprDt)
                    .build();
        } catch (Exception e) {
            log.warn("채권 데이터 변환 실패: {}", item, e);
            return BondDto.builder().build();
        }
    }

    private String convertDateFormat(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return null;
        }
        try {
            return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
        } catch (Exception e) {
            log.warn("날짜 형식 변환 실패: {}", dateStr, e);
            return null;
        }
    }

    private Double parseDouble(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            log.warn("숫자 변환 실패: {}", value, e);
            return null;
        }
    }
}