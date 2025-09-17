package com.fingrow.domain.financial.bond.controller;

import com.fingrow.domain.financial.bond.dto.BondSearchResponse;
import com.fingrow.domain.financial.bond.dto.BondSummaryDto;
import com.fingrow.domain.financial.bond.dto.BondTopResponse;
import com.fingrow.domain.financial.bond.service.BondService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bonds")
@RequiredArgsConstructor
@Slf4j
public class BondController {

    private final BondService bondService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncBondData(
            @RequestParam(value = "bondType", defaultValue = "금융채") String bondType) {
        try {
            log.info("채권 데이터 동기화 요청: {}", bondType);
            bondService.syncBondData(bondType);
            return ResponseEntity.ok("채권 데이터 동기화가 완료되었습니다: " + bondType);
        } catch (Exception e) {
            log.error("채권 데이터 동기화 실패", e);
            return ResponseEntity.internalServerError()
                    .body("채권 데이터 동기화 실패: " + e.getMessage());
        }
    }

    @GetMapping("/top")
    public ResponseEntity<BondTopResponse> getTopBonds(
            @RequestParam(value = "bondType", defaultValue = "금융채") String bondType,
            @RequestParam(value = "count", defaultValue = "5") Integer count) {
        try {
            log.info("상위 채권 조회 요청: {} ({}개)", bondType, count);
            BondTopResponse response = bondService.getTopBonds(bondType, count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상위 채권 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<BondSearchResponse> searchBonds(
            @RequestParam("keyword") String keyword) {
        try {
            log.info("채권 검색 요청: {}", keyword);
            BondSearchResponse response = bondService.searchBonds(keyword);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("채권 검색 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<BondSummaryDto>> getAllBonds() {
        try {
            log.info("전체 채권 목록 조회 요청");
            List<BondSummaryDto> bonds = bondService.getAllBonds();
            return ResponseEntity.ok(bonds);
        } catch (Exception e) {
            log.error("전체 채권 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/type/{bondType}")
    public ResponseEntity<List<BondSummaryDto>> getBondsByType(
            @PathVariable String bondType) {
        try {
            log.info("채권 종류별 조회 요청: {}", bondType);
            List<BondSummaryDto> bonds = bondService.getFutureBondsByType(bondType);
            return ResponseEntity.ok(bonds);
        } catch (Exception e) {
            log.error("채권 종류별 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/min-rate/{minRate}")
    public ResponseEntity<List<BondSummaryDto>> getBondsByMinRate(
            @PathVariable Double minRate) {
        try {
            log.info("최소 금리 이상 채권 조회 요청: {}%", minRate);
            List<BondSummaryDto> bonds = bondService.getBondsByMinRate(minRate);
            return ResponseEntity.ok(bonds);
        } catch (Exception e) {
            log.error("최소 금리 이상 채권 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<String> checkApiStatus() {
        return ResponseEntity.ok("Bond API is running");
    }
}