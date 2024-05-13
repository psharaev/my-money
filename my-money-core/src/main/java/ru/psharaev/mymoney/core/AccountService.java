package ru.psharaev.mymoney.core;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.psharaev.mymoney.core.entity.Account;
import ru.psharaev.mymoney.core.entity.Currency;
import ru.psharaev.mymoney.core.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final FlowRepository flowRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Account createAccount(long userId, String name, Currency currency) {
        return accountRepository.save(Account.builder()
                .ownerUserId(userId)
                .name(name)
                .currency(currency)
                .build()
        );
    }

    @Transactional
    public void renameAccount(long accountId, String newName) {
        accountRepository.renameAccount(accountId, newName);
    }

    public Optional<Long> getFavoriteAccountId(long userId) {
        return userRepository.getFavoriteAccountId(userId);
    }

    public List<Account> getAllAccounts(long userId) {
        return accountRepository.findAllByOwnerUserId(userId);
    }

    public Account getAccount(long accountId) {
        return accountRepository.findById(accountId).orElseThrow(NoSuchElementException::new);
    }

    public BigDecimal calcBalance(long accountId) {
        BigDecimal res = accountRepository.calcAccountBalance(accountId);
        if (res == null) {
            return BigDecimal.ZERO;
        }
        return res;
    }

    @Transactional
    public void deleteAccount(long accountId) {
        flowRepository.deleteAllByAccountId(accountId);
        transactionRepository.deleteAllByFromAccountIdOrToAccountId(accountId, accountId);
        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isPresent()) {
            userRepository.uncheckFavoriteAccount(accountOptional.get().getOwnerUserId(), accountId);
        }
        accountRepository.deleteById(accountId);
    }
}
