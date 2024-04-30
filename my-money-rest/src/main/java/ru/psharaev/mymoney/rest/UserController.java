package ru.psharaev.mymoney.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.psharaev.mymoney.core.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Service

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
public class UserController {
    private final UserService userService;

    @DeleteMapping("delete/{userId}")
    public ResponseEntity<String> deleteUser(
            @PathVariable("userId") long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity
                .noContent()
                .build();
    }
}
