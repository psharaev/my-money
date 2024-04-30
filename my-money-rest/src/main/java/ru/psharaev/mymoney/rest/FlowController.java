package ru.psharaev.mymoney.rest;

import lombok.RequiredArgsConstructor;
import ru.psharaev.mymoney.rest.api.CreateFlowRequest;
import ru.psharaev.mymoney.core.FlowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/account/flow")
public class FlowController {
    private final FlowService flowService;

    @PostMapping("create")
    public ResponseEntity<Object> createFlow(
            @RequestBody CreateFlowRequest request
    ) {
        flowService.createFlow(
                request.getAccountId(),
                request.getAmount(),
                request.getTime(),
                request.getCategory().trim(),
                request.getDescription().trim()
        );
        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("delete/{flowId}")
    public ResponseEntity<Object> deleteFlow(
            @PathVariable("flowId") long flowId
    ) {
        flowService.deleteFlow(flowId);
        return ResponseEntity
                .noContent()
                .build();
    }
}
