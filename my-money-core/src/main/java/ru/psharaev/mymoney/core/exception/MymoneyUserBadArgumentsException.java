package ru.psharaev.mymoney.core.exception;

public class MymoneyUserBadArgumentsException extends MymoneyException {
    MymoneyUserBadArgumentsException(String message) {
        super(message);
    }

    MymoneyUserBadArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }
}
