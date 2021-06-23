package com.wushiyii.utils;

import com.wushiyii.BaseTest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CodeAndDescTest extends BaseTest {




    @AllArgsConstructor
    @Getter
    public enum TestIntegerEnum {

        ONE(1, "THIS IS 1"),
        TWO(1, "THIS IS 2");

        private final Integer code;
        private final String desc;
    }

    @AllArgsConstructor
    @Getter
    public enum TestLongEnum {

        ONE(15234523452134L, "THIS IS 15234523452134L"),
        TWO(53245234523452L, "THIS IS 53245234523452L");

        private final Long code;
        private final String desc;
    }

    @Test
    public void ofTest() {

        List<CodeAndDesc<Integer>> integerList = Arrays.stream(TestIntegerEnum.values()).map(testEnum -> CodeAndDesc.of(testEnum.code, testEnum.desc)).collect(Collectors.toList());
        List<CodeAndDesc<Long>> longList = Arrays.stream(TestLongEnum.values()).map(testEnum -> CodeAndDesc.of(testEnum.code, testEnum.desc)).collect(Collectors.toList());
        print(integerList);
        print(longList);

    }


}
