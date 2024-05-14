package ru.psharaev.mymoney.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.psharaev.mymoney.core.entity.Flow;
import ru.psharaev.mymoney.core.entity.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    @Transactional
    public Transaction createTransaction(long fromAccountId, long toAccountId, BigDecimal fromAmount, BigDecimal toAmount, OffsetDateTime time, String category, String description) {
        Transaction transaction = Transaction.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .fromAmount(fromAmount)
                .toAmount(toAmount)
                .time(time.toInstant())
                .categoryId(categoryService.getOrCreateCategory(category).getCategoryId())
                .description(description)
                .build();
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions(long userId) {
        return transactionRepository.findAllByUserId(userId);
    }
}
