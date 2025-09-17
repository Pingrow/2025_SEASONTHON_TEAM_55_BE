package com.fingrow.domain.financial.bond.repository;

import com.fingrow.domain.financial.bond.entity.Bond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BondRepository extends JpaRepository<Bond, Long> {
    
    Optional<Bond> findByIsinCd(String isinCd);
    
    List<Bond> findByBondIsurNmContainingIgnoreCase(String issuerName);
    
    List<Bond> findByIsinCdNmContainingIgnoreCase(String bondName);
    
    List<Bond> findByScrsItmsKcdNm(String bondType);
    
    @Query("SELECT b FROM Bond b WHERE b.bondExprDt >= :currentDate ORDER BY b.bondSrfcInrt DESC")
    List<Bond> findFutureBondsOrderByInterestRateDesc(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT b FROM Bond b WHERE b.bondExprDt >= :currentDate ORDER BY b.bondExprDt ASC")
    List<Bond> findFutureBondsOrderByMaturityDateAsc(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT b FROM Bond b WHERE b.bondExprDt >= :currentDate AND b.scrsItmsKcdNm = :bondType ORDER BY b.bondSrfcInrt DESC")
    List<Bond> findFutureBondsByTypeOrderByInterestRateDesc(
            @Param("currentDate") LocalDate currentDate, 
            @Param("bondType") String bondType);
    
    @Query("SELECT b FROM Bond b WHERE b.bondExprDt >= :currentDate AND b.bondSrfcInrt >= :minRate ORDER BY b.bondSrfcInrt DESC")
    List<Bond> findFutureBondsByMinRateOrderByInterestRateDesc(
            @Param("currentDate") LocalDate currentDate, 
            @Param("minRate") Double minRate);
    
    @Query("SELECT b FROM Bond b WHERE b.bondExprDt BETWEEN :startDate AND :endDate ORDER BY b.bondSrfcInrt DESC")
    List<Bond> findBondsByMaturityPeriodOrderByInterestRateDesc(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}