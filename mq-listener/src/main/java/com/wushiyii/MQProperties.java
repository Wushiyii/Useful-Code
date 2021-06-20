package com.wushiyii;

import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import lombok.Data;

@Data
public class MQProperties {

    private String consumeGroup;
    private String namesrvAddr;
    private String instanceName;
    private ConsumeFromWhere consumeFromWhere;
    private MessageModel messageModel;
    private Integer consumeThreadMax;
    private Integer consumeThreadMin;
    private Integer messageBatchMaxSize;


}
