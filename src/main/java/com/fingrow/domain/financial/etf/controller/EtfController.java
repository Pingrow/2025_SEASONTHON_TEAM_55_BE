package com.fingrow.domain.financial.etf.controller;

import com.fingrow.domain.financial.etf.dto.EtfDto;
import com.fingrow.domain.financial.etf.service.EtfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/etf")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ETF API", description = "ETF 상품 및 시세 관리 API")
public class EtfController {

    private final EtfService etfService;

    // =========================== 동기화 ===========================
    @PostMapping("/sync")
    @Operation(
            summary = "ETF 데이터 동기화",
            description = "KRX API(getETFPriceInfo)를 호출해 ETF 상품과 시세 정보를 모두 동기화합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "동기화 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류"),
            @ApiResponse(responseCode = "503", description = "외부 API 서비스 이용 불가")
    })
    public ResponseEntity<EtfDto.ApiResponse<EtfDto.SyncResponse>> syncEtfData() {
        log.info("ETF 데이터 동기화 시작");

        try {
            EtfDto.SyncResponse syncResponse = etfService.syncAllEtfData();

            // 부분 실패인 경우와 완전 성공인 경우 구분
            if (syncResponse.getFailureCount() > 0 && syncResponse.getSuccessCount() == 0) {
                // 완전 실패
                log.error("ETF 동기화 완전 실패 - 성공: {}개, 실패: {}개",
                        syncResponse.getSuccessCount(), syncResponse.getFailureCount());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(EtfDto.ApiResponse.error("ETF 동기화에 실패했습니다."));
            } else if (syncResponse.getFailureCount() > 0) {
                // 부분 실패
                log.warn("ETF 동기화 부분 실패 - 성공: {}개, 실패: {}개",
                        syncResponse.getSuccessCount(), syncResponse.getFailureCount());
                String message = String.format("ETF 동기화 부분 완료 - 성공: %d개, 실패: %d개 (일부 데이터 처리 실패)",
                        syncResponse.getSuccessCount(), syncResponse.getFailureCount());
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .body(EtfDto.ApiResponse.success(message, syncResponse));
            } else {
                // 완전 성공
                log.info("ETF 동기화 완료 - 성공: {}개", syncResponse.getSuccessCount());
                String message = String.format("ETF 동기화 완료 - 성공: %d개", syncResponse.getSuccessCount());
                return ResponseEntity.ok(EtfDto.ApiResponse.success(message, syncResponse));
            }

        } catch (ResourceAccessException e) {
            log.error("KRX API 연결 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(EtfDto.ApiResponse.error("외부 API 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해주세요."));
        } catch (RestClientException e) {
            log.error("KRX API 호출 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(EtfDto.ApiResponse.error("외부 API 호출 중 오류가 발생했습니다: " + e.getMessage()));
        } catch (Exception e) {
            log.error("ETF 동기화 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EtfDto.ApiResponse.error("ETF 동기화에 실패했습니다: " + e.getMessage()));
        }
    }

    // =========================== 조회 ===========================
    @GetMapping
    @Operation(
            summary = "ETF 상품 목록 조회",
            description = "모든 ETF 상품 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "데이터 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<EtfDto.ApiResponse<EtfDto.EtfListResponse>> getEtfProducts() {
        log.info("ETF 상품 목록 조회 요청");

        try {
            EtfDto.EtfListResponse response = etfService.getAllEtfProducts();

            if (response.getTotalCount() == 0) {
                log.info("조회된 ETF 상품이 없음");
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(EtfDto.ApiResponse.success("조회된 ETF 상품이 없습니다.", response));
            }

            log.info("ETF 상품 {}개 조회 완료", response.getTotalCount());
            String message = String.format("%d개의 ETF 상품을 조회했습니다.", response.getTotalCount());
            return ResponseEntity.ok(EtfDto.ApiResponse.success(message, response));

        } catch (Exception e) {
            log.error("ETF 상품 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EtfDto.ApiResponse.error("ETF 상품 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    // =========================== 헬스 체크 ===========================
    @GetMapping("/health")
    @Operation(
            summary = "서비스 상태 확인",
            description = "ETF 서비스의 현재 상태를 확인합니다."
    )
    public ResponseEntity<EtfDto.ApiResponse<String>> healthCheck() {
        log.debug("ETF 서비스 헬스체크 요청");
        return ResponseEntity.ok(EtfDto.ApiResponse.success("ETF 서비스가 정상 작동 중입니다."));
    }

    @GetMapping("/etf")
    @Operation(
            summary = "ETF 간단 목록 조회 (리스트용)",
            description = "FE 리스트 화면에 필요한 핵심 정보만 조회합니다. (이름, 가격, 등락률, 거래량, 시총)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "데이터 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<EtfDto.ApiResponse<EtfDto.EtfSimpleListResponse>> getEtfSimpleList() {
        log.info("ETF 간단 목록 조회 요청");

        try {
            EtfDto.EtfSimpleListResponse response = etfService.getEtfSimpleList();

            if (response.getTotalCount() == 0) {
                log.info("조회된 ETF 데이터가 없음");
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(EtfDto.ApiResponse.success("조회된 ETF 데이터가 없습니다.", response));
            }

            log.info("ETF {}개 간단 목록 조회 완료 (기준일: {})", response.getTotalCount(), response.getBaseDate());
            String message = String.format("%d개의 ETF 목록을 조회했습니다. (기준일: %s)",
                    response.getTotalCount(), response.getBaseDate());
            return ResponseEntity.ok(EtfDto.ApiResponse.success(message, response));

        } catch (Exception e) {
            log.error("ETF 간단 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EtfDto.ApiResponse.error("ETF 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}