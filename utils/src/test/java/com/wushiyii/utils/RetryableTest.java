package com.wushiyii.utils;

import com.wushiyii.BaseTest;
import org.junit.Test;



public class RetryableTest extends BaseTest {

    @Test
    public void testRetry() {

//        Retryable.of(() -> 0 / 10, 10, 1000).retry();
        Retryable.of(() -> 10 / 0).retry();

    }


}
