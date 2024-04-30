package ru.psharaev.mymoney.rest;

import lombok.RequiredArgsConstructor;
import ru.psharaev.mymoney.core.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/account")
public class AccountController {
    private final AccountService accountService;

    @DeleteMapping("delete/{accountId}")
    public ResponseEntity<String> deleteAccount(
            @PathVariable("accountId") long accountId
    ) {
        accountService.deleteAccount(accountId);
        return ResponseEntity
                .noContent()
                .build();
    }
}
