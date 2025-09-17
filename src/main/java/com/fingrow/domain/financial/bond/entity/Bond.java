package com.fingrow.domain.financial.bond.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "bonds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bond {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isin_cd", unique = true, nullable = false, length = 50)
    private String isinCd; // ISIN 코드

    @Column(name = "isin_cd_nm", length = 200)
    private String isinCdNm; // 종목명

    @Column(name = "bond_isur_nm", length = 100)
    private String bondIsurNm; // 발행회사명

    @Column(name = "bond_srfc_inrt")
    private Double bondSrfcInrt; // 금리(%)

    @Column(name = "bond_expr_dt")
    private LocalDate bondExprDt; // 만기일

    @Column(name = "scrs_itms_kcd_nm", length = 50)
    private String scrsItmsKcdNm; // 유가증권종목종류코드명 (금융채 등)

    @Column(name = "bas_dt")
    private String basDt; // 기준일

    @Column(name = "created_at")
    private LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }

    public Integer getDaysToMaturity() {
        if (bondExprDt == null) return null;
        LocalDate today = LocalDate.now();
        return (int) java.time.temporal.ChronoUnit.DAYS.between(today, bondExprDt);
    }

    public boolean isMatured() {
        if (bondExprDt == null) return false;
        return bondExprDt.isBefore(LocalDate.now());
    }
}