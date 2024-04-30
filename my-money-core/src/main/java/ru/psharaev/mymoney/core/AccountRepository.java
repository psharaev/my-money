package ru.psharaev.mymoney.core;

import ru.psharaev.mymoney.core.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

interface AccountRepository extends JpaRepository<Account, Long> {
    void deleteAllByOwnerUserId(long userId);

    @Query(value = """
            select coalesce((select sum(amount) from mymoney.flows where account_id = :accountId), 0) +
                   coalesce((select sum(to_amount) from mymoney.transactions where to_account_id = :accountId), 0)
            """, nativeQuery = true)
    BigDecimal calcAccountBalance(@Param("accountId") long accountId);
}
