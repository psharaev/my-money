package ru.psharaev.mymoney.core;

import ru.psharaev.mymoney.core.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

interface TransactionRepository extends JpaRepository<Transaction, Long> {
    void deleteAllByFromAccountIdOrToAccountId(long fromAccountId, long toAccountId);
}
