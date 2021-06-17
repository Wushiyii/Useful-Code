package com.wushiyii.beans;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

@Slf4j
public class Retryable<T> {

    private final Supplier<T> supplier;

    private final int retryMaxCount;

    private final long retryTime;

    private int alreadyRetriedCount = 0;

    private static final int DEFAULT_RETRY_MAX_COUNT = 10;
    private static final long DEFAULT_RETRY_TIME = 100L;

    public Retryable(Supplier<T> supplier, int retryMaxCount, long retryTime) {
        this.supplier = supplier;
        this.retryMaxCount = retryMaxCount;
        this.retryTime = retryTime;
    }


    public static <T> Retryable<T> of (Supplier<T> supplier) {
        return of(supplier, DEFAULT_RETRY_MAX_COUNT, DEFAULT_RETRY_TIME);
    }

    public static <T> Retryable<T> of (Supplier<T> supplier, int retryMaxCount) {
        return of(supplier, retryMaxCount, DEFAULT_RETRY_TIME);
    }

    public static <T> Retryable<T> of (Supplier<T> supplier, int retryMaxCount, long retryTime) {
        return new Retryable<>(supplier, retryMaxCount, retryTime);
    }

    public void retry() {

        try {
            this.supplier.get();
        } catch (Exception e) {
            log.error("retry occur error alreadyRetriedCount={}, retryMaxCount={}", alreadyRetriedCount, retryMaxCount, e);

            if (alreadyRetriedCount++ < retryMaxCount) {
                if (retryTime > 0) {
                    LockSupport.parkNanos(retryTime);
                }
                retry();
            }
            throw new RuntimeException("retry count over maxRetryCount (" + retryMaxCount  + ")", e);
        }

    }

}
