package com.wushiyii.beans;

import com.wushiyii.BaseTest;
import org.junit.Test;

public class PairTest extends BaseTest {

    @Test
    public void ofTest() {
        print(Pair.of("Hello", "ABC"));
    }

}
