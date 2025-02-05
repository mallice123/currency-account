package com.portfolio.recruitment.currencyaccount.connectors.db.repository;

import com.portfolio.recruitment.currencyaccount.connectors.db.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.id = :accountId")
    Optional<AccountEntity> findByIdForUpdate(@Param("accountId") Long accountId);
}
