package com.fingrow.domain.financial.bond.controller;

import com.fingrow.domain.financial.bond.dto.BondResponse;
import com.fingrow.domain.financial.bond.service.BondService;
import com.fingrow.domain.financial.deposit.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/financial/bond")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "채권 API", description = "금융채 상품 조회 API")
public class BondController {

    private final BondService bondService;

    @GetMapping
    @Operation(
            summary = "채권 정보 조회",
            description = "금융채 상품의 금리 TOP5와 만기 TOP5 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<CommonResponse<BondResponse>> getBondInfo() {
        try {
            log.info("채권 정보 조회 요청");
            BondResponse bondInfo = bondService.getBondInfo();
            return ResponseEntity.ok(
                    CommonResponse.success(bondInfo.getMessage(), bondInfo)
            );
        } catch (Exception e) {
            log.error("채권 정보 조회 실패", e);
            return ResponseEntity.internalServerError().body(
                    CommonResponse.error("채권 정보 조회에 실패했습니다: " + e.getMessage())
            );
        }
    }
}