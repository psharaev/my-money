package ru.psharaev.mymoney.core.exception;

public class MymoneyException extends RuntimeException {
    MymoneyException(String message) {
        super(message);
    }

    MymoneyException(String message, Throwable cause) {
        super(message, cause);
    }

    public static MymoneyException userBadArguments(String message) {
        return new MymoneyUserBadArgumentsException(message);
    }

    public static MymoneyException userBadArguments(Throwable e, String message) {
        return new MymoneyUserBadArgumentsException(message, e);
    }
}
