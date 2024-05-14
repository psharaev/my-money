package ru.psharaev.mymoney.core;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.psharaev.mymoney.core.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface TransactionRepository extends JpaRepository<Transaction, Long> {
    void deleteAllByFromAccountIdOrToAccountId(long fromAccountId, long toAccountId);

    @Query(value = """
            select t
            from Transaction t inner join Account a on (t.fromAccountId = a.accountId and a.ownerUserId = :userId) or
                        (t.toAccountId = a.accountId and a.ownerUserId = :userId)
            where a.ownerUserId = :userId
            """)
    List<Transaction> findAllByUserId(@Param("userId") long userId);
}
