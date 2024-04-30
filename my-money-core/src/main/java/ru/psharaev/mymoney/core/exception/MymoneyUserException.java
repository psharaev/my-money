package ru.psharaev.mymoney.core.exception;

public class MymoneyUserException extends MymoneyException {
    MymoneyUserException(String message) {
        super(message);
    }

    MymoneyUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
