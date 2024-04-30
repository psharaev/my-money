package ru.psharaev.mymoney.core;

import ru.psharaev.mymoney.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = """
            update User
            set favoriteAccountId = 0
            where userId = :userId
                and favoriteAccountId = :accountId
            """)
    @Modifying
    void uncheckFavoriteAccount(@Param("userId") long userId, @Param("accountId") long accountId);

    Optional<User> findByTelegramChatId(long telegramChatId);
}
