package com.wushiyii;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MQListener {

    String topic();

    String[] tags();
}
