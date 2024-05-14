package ru.psharaev.mymoney.core;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.psharaev.mymoney.core.entity.Flow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface FlowRepository extends JpaRepository<Flow, Long> {
    void deleteAllByAccountId(long accountId);

    @Query(value = """
            select f
            from Flow f inner join Account a on f.accountId = a.accountId
            where a.ownerUserId = :userId
            """)
    List<Flow> findAllByUserId(@Param("userId") long userId);
}
