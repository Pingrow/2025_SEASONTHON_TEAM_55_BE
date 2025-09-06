package com.fingrow.domain.financial.repository;

import com.fingrow.domain.financial.entity.DepositProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepositProductRepository extends JpaRepository<DepositProduct, Long> {
    // 은행명으로 검색
    List<DepositProduct> findByKorCoNmContaining(String bankName);

    // 상품명으로 검색
    List<DepositProduct> findByFinPrdtNmContaining(String productName);

    // 특정 기간의 상품들을 최고금리 순으로 조회
    @Query("SELECT d FROM DepositProduct d " +
            "JOIN d.options o " +
            "WHERE o.saveTrm = :term " +
            "ORDER BY o.intrRate2 DESC")
    List<DepositProduct> findByTermOrderByBestRateDesc(@Param("term") Integer term);
}
