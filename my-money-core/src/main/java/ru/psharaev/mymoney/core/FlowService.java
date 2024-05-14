package ru.psharaev.mymoney.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.psharaev.mymoney.core.entity.Flow;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowService {
    private final FlowRepository flowRepository;
    private final CategoryService categoryService;

    @Transactional
    public void createFlow(long accountId, BigDecimal amount, OffsetDateTime time, String category, String description) {
        Flow flow = Flow.builder()
                .accountId(accountId)
                .amount(amount)
                .time(time.toInstant())
                .categoryId(categoryService.getOrCreateCategory(category).getCategoryId())
                .description(description)
                .build();

        flowRepository.save(flow);
    }

    public List<Flow> getAllFlows(long userId) {
        return flowRepository.findAllByUserId(userId);
    }

    public void deleteFlow(long flowId) {
        flowRepository.deleteById(flowId);
    }
}
