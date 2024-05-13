package ru.psharaev.mymoney.core;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.psharaev.mymoney.core.entity.Category;
import ru.psharaev.mymoney.core.entity.Account;
import ru.psharaev.mymoney.core.entity.Currency;
import ru.psharaev.mymoney.core.entity.User;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class UserService extends Service<UserService> {
    private final UserRepository userRepository;
    private final FlowRepository flowRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final AccountRepository accountRepository;

    public User getUser(long userId) {
        return userRepository.findById(userId).orElseThrow(NoSuchElementException::new);
    }

    public User getOrCreateTelegramUser(long telegramChatId, String languageCode, ZoneOffset zoneOffset) {
        return userRepository.findByTelegramChatId(telegramChatId)
                .orElseGet(() -> getProxy().createByTelegram(telegramChatId, languageCode, zoneOffset));
    }

    @Transactional
    protected User createByTelegram(long telegramChatId, String languageCode, ZoneOffset zoneOffset) {
        User user = userRepository.save(User.builder()
                .createdAt(Instant.now())
                .lastOperationAt(Instant.now())
                .languageCode(languageCode)
                .timezone(zoneOffset)
                .telegramChatId(telegramChatId)
                .build()
        );

        Account account = accountRepository.save(Account.builder()
                .ownerUserId(user.getUserId())
                .name("Мой первый счёт")
                .currency(Currency.RUB)
                .build()
        );

        Category category = categoryService.getOrCreateCategory("Прочее");
        user.setFavoriteAccountId(account.getAccountId());
        user.setFavoriteCategoryFlowId(category.getCategoryId());
        user.setFavoriteCategoryTransactionId(category.getCategoryId());

        return userRepository.save(user);
    }

    @Transactional
    public void setUserFavoriteAccount(long userId, long accountId) {
        userRepository.setUserFavoriteAccountId(userId, accountId);
    }

    @Transactional
    public void deleteUser(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NoSuchElementException("Not found user with id: " + userId);
        }

        User user = userOptional.get();
        List<Account> accounts = user.getAccounts();
        for (Account account : accounts) {
            flowRepository.deleteAllByAccountId(account.getAccountId());
            transactionRepository.deleteAllByFromAccountIdOrToAccountId(account.getAccountId(), account.getAccountId());
        }
        accountRepository.deleteAllByOwnerUserId(userId);
        userRepository.deleteById(userId);
    }
}
