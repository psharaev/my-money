package ru.psharaev.mymoney.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.psharaev.mymoney.core.entity.User;

import java.util.Optional;

interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = """
            update User
            set favoriteAccountId = null
            where userId = :userId
                and favoriteAccountId = :accountId
            """)
    @Modifying
    void uncheckFavoriteAccount(@Param("userId") long userId, @Param("accountId") long accountId);

    @Query(value = """
            select favoriteAccountId
            from User
            where userId = :userId
            """)
    Optional<Long> getFavoriteAccountId(@Param("userId") long userId);

    @Query(value = """
            update User
            set favoriteAccountId = :accountId
            where userId = :userId
            """)
    @Modifying
    void setUserFavoriteAccountId(@Param("userId") long userId, @Param("accountId") long accountId);

    Optional<User> findByTelegramChatId(long telegramChatId);
}
