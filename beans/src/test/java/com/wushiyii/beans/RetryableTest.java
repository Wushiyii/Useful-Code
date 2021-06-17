package com.wushiyii.beans;

import com.wushiyii.BaseTest;
import org.junit.Test;


public class RetryableTest extends BaseTest {

    @Test
    public void testRetry() {

        Retryable.of(() -> 0 / 10).retry();
        Retryable.of(() -> 10 / 0).retry();

    }


}
