package ru.psharaev.mymoney.core;

import org.springframework.aop.framework.AopContext;

abstract class Service<T> {
    protected final T getProxy() {
        return (T) AopContext.currentProxy();
    }
}
