package com.fingrow.domain.financial.etf.repository;

import com.fingrow.domain.financial.etf.entity.EtfProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EtfProductRepository extends JpaRepository<EtfProduct, Long> {

    // 기본 조회
    List<EtfProduct> findByBasDt(String basDt);

    Optional<EtfProduct> findBySrtnCdAndBasDt(String srtnCd, String basDt);

    // 최신 날짜 조회
    @Query("SELECT MAX(e.basDt) FROM EtfProduct e")
    String findLatestBasDt();

    // 최신 날짜의 모든 ETF 조회
    @Query("SELECT e FROM EtfProduct e WHERE e.basDt = (SELECT MAX(ep.basDt) FROM EtfProduct ep)")
    List<EtfProduct> findAllWithLatestDate();

}