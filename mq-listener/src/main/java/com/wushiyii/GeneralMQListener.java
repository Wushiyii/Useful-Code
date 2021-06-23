package com.wushiyii;

import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.common.message.MessageExt;

public interface GeneralMQListener<BODY> extends MessageListener {

    boolean consume(MessageExt msg, BODY body);

}
