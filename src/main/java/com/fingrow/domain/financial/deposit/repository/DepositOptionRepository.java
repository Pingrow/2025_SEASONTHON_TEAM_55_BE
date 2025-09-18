package com.fingrow.domain.financial.deposit.repository;

import com.fingrow.domain.financial.deposit.entity.DepositOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepositOptionRepository extends JpaRepository<DepositOption, Long> {

}
