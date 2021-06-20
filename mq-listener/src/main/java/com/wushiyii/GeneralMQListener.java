package com.wushiyii;

import com.alibaba.rocketmq.common.message.MessageExt;

public interface GeneralMQListener<BODY> {

    boolean consume(MessageExt msg, BODY body);

}
