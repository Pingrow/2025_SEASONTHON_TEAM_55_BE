package com.fingrow.domain.onboard.repository;

import com.fingrow.domain.onboard.entity.InvestmentPreference;
import com.fingrow.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvestmentPreferenceRepository extends JpaRepository<InvestmentPreference, Long> {
    
    Optional<InvestmentPreference> findByUserId(Long userId);
    
    Optional<InvestmentPreference> findByUser(User user);
    
    boolean existsByUserId(Long userId);
    
    void deleteByUserId(Long userId);
    
    @Query("SELECT ip FROM InvestmentPreference ip " +
           "JOIN FETCH ip.user " +
           "WHERE ip.user.id = :userId")
    Optional<InvestmentPreference> findByUserIdWithUser(@Param("userId") Long userId);
}