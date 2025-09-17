package com.fingrow.domain.financial.deposit.repository;

import com.fingrow.domain.financial.deposit.entity.SavingProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SavingProductRepository extends JpaRepository<SavingProduct, Long> {
    // 은행명으로 검색
    List<SavingProduct> findByKorCoNmContaining(String bankName);

    // 상품명으로 검색
    List<SavingProduct> findByFinPrdtNmContaining(String productName);

    // 특정 기간의 상품들을 최고금리 순으로 조회
    @Query("SELECT s FROM SavingProduct s " +
            "JOIN s.options o " +
            "WHERE o.saveTrm = :term " +
            "ORDER BY o.intrRate2 DESC")
    List<SavingProduct> findByTermOrderByBestRateDesc(@Param("term") Integer term);
}
