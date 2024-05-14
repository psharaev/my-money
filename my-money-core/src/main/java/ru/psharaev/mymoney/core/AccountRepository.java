package ru.psharaev.mymoney.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.psharaev.mymoney.core.entity.Account;

import java.math.BigDecimal;
import java.util.List;

interface AccountRepository extends JpaRepository<Account, Long> {
    void deleteAllByOwnerUserId(long userId);

    List<Account> findAllByOwnerUserId(long userId);

    @Query(value = """
            select coalesce((select sum(amount) from mymoney.flows where account_id = :accountId), 0) +
                   coalesce((select -sum(from_amount) from mymoney.transactions where from_account_id = :accountId), 0) +
                   coalesce((select sum(to_amount) from mymoney.transactions where to_account_id = :accountId), 0)
            """, nativeQuery = true)
    BigDecimal calcAccountBalance(@Param("accountId") long accountId);

    @Query(value = """
            update Account
            set name = :newName
            where accountId = :accountId
            """)
    @Modifying
    void renameAccount(@Param("accountId") long accountId, @Param("newName") String newName);
}
