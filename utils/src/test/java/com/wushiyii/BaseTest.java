package com.wushiyii;

import com.alibaba.fastjson.JSON;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Objects;

@RunWith(JUnit4.class)
public class BaseTest {

    protected void print(Object obj) {
        if (Objects.nonNull(obj)) {
            System.out.println(">>>>");
            System.out.println(JSON.toJSONString(obj));
            System.out.println("<<<<");
        }
    }


}
