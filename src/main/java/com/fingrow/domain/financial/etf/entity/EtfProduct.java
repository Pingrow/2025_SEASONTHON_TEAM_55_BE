package com.fingrow.domain.financial.etf.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "etf_products", indexes = {
        @Index(name = "idx_etf_srtn_cd_bas_dt", columnList = "srtn_cd, bas_dt"),
        @Index(name = "idx_etf_bas_dt", columnList = "bas_dt"),
        @Index(name = "idx_etf_mrkt_ctg", columnList = "mrkt_ctg")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtfProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== 상품 정보 ==========
    @Column(name = "srtn_cd", nullable = false, length = 20)
    private String srtnCd; // 종목코드

    @Column(name = "isin_cd", length = 12)
    private String isinCd; // ISIN코드

    @Column(name = "itms_nm", length = 200, nullable = false)
    private String itmsNm; // 종목명 (ETF명)

    @Column(name = "mrkt_ctg", length = 10)
    private String mrktCtg; // 시장구분 (KOSPI, KOSDAQ)

    @Column(name = "corp_nm", length = 100)
    private String corpNm; // 운용사명

    // ========== 시세 정보 ==========
    @Column(name = "bas_dt", length = 8, nullable = false)
    private String basDt; // 기준일자 (YYYYMMDD)

    @Column(name = "clpr")
    private Long clpr; // 종가

    @Column(name = "vs", length = 20)
    private String vs; // 전일대비

    @Column(name = "flt_rt", length = 10)
    private String fltRt; // 등락률

    @Column(name = "mkp")
    private Long mkp; // 시가

    @Column(name = "hipr")
    private Long hipr; // 고가

    @Column(name = "lopr")
    private Long lopr; // 저가

    @Column(name = "trqu")
    private Long trqu; // 거래량

    @Column(name = "tr_prc")
    private Long trPrc; // 거래대금

    @Column(name = "lstg_st_cnt")
    private Long lstgStCnt; // 상장주식수

    @Column(name = "mrkt_tot_amt")
    private Long mrktTotAmt; // 시가총액

    // ========== 메타 정보 ==========
    @Column(name = "created_at", updatable = false)
    private String createdAt; // 생성일시 (String으로 저장)

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now().toString();
    }
}