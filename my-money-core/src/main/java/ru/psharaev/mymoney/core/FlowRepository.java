package ru.psharaev.mymoney.core;

import ru.psharaev.mymoney.core.entity.Flow;
import org.springframework.data.jpa.repository.JpaRepository;

interface FlowRepository extends JpaRepository<Flow, Long> {
    void deleteAllByAccountId(long accountId);
}
