package com.wushiyii;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.*;

@Slf4j
@Data
public class ListenerRegister implements DisposableBean {


    private Map<String, String> topicAndTagMap = new HashMap<>();
    private Map<String /* topic*/, ListenerWrapperMap> listenerWrapperMap = new HashMap<>();
    private DefaultMQPushConsumer consumer;

    /* MQ internal properties */
    private String consumeGroup;
    private String namesrvAddr;
    private String instanceName;
    private ConsumeFromWhere consumeFromWhere;
    private MessageModel messageModel;
    private Integer consumeThreadMin;
    private Integer consumeThreadMax;
    private Integer consumeMessageBatchMaxSize;

    private static final String DEFAULT_GROUP_NAME = "DEFAULT_GROUP";
    private static final String DEFAULT_INSTANCE_NAME = "DEFAULT_INSTANCE";
    private static final Integer DEFAULT_BATCH_SIZE = 1;
    private static final Integer DEFAULT_CONSUME_THREAD_MAX = 16;
    private static final Integer DEFAULT_CONSUME_THREAD_MIN = 4;
    private static final ConsumeFromWhere DEFAULT_CONSUME_FROM = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
    private static final MessageModel DEFAULT_MESSAGE_MODEL = MessageModel.CLUSTERING;


    public ListenerRegister(MQProperties mqProperties) {
        this.namesrvAddr = Validate.notBlank(mqProperties.getNamesrvAddr(), "Init ListenerRegister, the namesrcAddr could not be null or blank");
        this.consumeGroup = StringUtils.defaultString(mqProperties.getConsumeGroup(), DEFAULT_GROUP_NAME);
        this.instanceName = StringUtils.defaultString(mqProperties.getInstanceName(), DEFAULT_INSTANCE_NAME);
        this.consumeFromWhere = Objects.nonNull(mqProperties.getConsumeFromWhere()) ? mqProperties.getConsumeFromWhere() : DEFAULT_CONSUME_FROM;
        this.messageModel = Objects.nonNull(mqProperties.getMessageModel()) ? mqProperties.getMessageModel() : DEFAULT_MESSAGE_MODEL;
        this.consumeThreadMax = Objects.nonNull(mqProperties.getConsumeThreadMax()) ? mqProperties.getConsumeThreadMax() : DEFAULT_CONSUME_THREAD_MAX;
        this.consumeThreadMin = Objects.nonNull(mqProperties.getConsumeThreadMin()) ? mqProperties.getConsumeThreadMin() : DEFAULT_CONSUME_THREAD_MIN;
        this.consumeMessageBatchMaxSize = Objects.nonNull(mqProperties.getConsumeMessageBatchMaxSize()) ? mqProperties.getConsumeMessageBatchMaxSize() : DEFAULT_BATCH_SIZE;
    }


    public void init(ApplicationContext applicationContext) {

        String[] beanNames = applicationContext.getBeanNamesForAnnotation(MQListener.class);
        for (String beanName : beanNames) {
            GeneralMQListener<?> matchedMQListener = applicationContext.getBean(beanName, GeneralMQListener.class);
            Validate.notNull(matchedMQListener, "The class " + beanName+ ", annotation with @MQListener, doesn't implements with GeneralMQListener");

            MQListener mqListener = AnnotationUtils.findAnnotation(matchedMQListener.getClass(), MQListener.class);
            validateAndAdd(mqListener, matchedMQListener);
        }
        initConsumer();
    }

    private void validateAndAdd(MQListener mqListener, GeneralMQListener<?> matchedMQListener) {

        String topic = Validate.notBlank(mqListener.topic(), "Topic cannot be blank");
        String[] tags = Validate.notEmpty(mqListener.tags(), "Tags cannot be empty");

        if (!listenerWrapperMap.containsKey(topic)) {
            listenerWrapperMap.put(topic, new ListenerWrapperMap());
        }
        ListenerWrapperMap listenerWrapperMap = this.listenerWrapperMap.get(topic);

        for (String tag : tags) {
            if (!listenerWrapperMap.containsKey(tag)) {
                listenerWrapperMap.put(tag, new LinkedHashSet<>());
            }
            listenerWrapperMap.get(tag).add(new ListenerWrapper(matchedMQListener, ResolvableType.forClass(matchedMQListener.getClass()).as(GeneralMQListener.class).getGeneric(0).resolve()));
        }

    }


    private void initConsumer() {

        Validate.notEmpty(listenerWrapperMap, "listener map is empty, please check the usage");

        Map<String, String> subscription = new HashMap<>();

        for (Map.Entry<String, ListenerWrapperMap> entry : listenerWrapperMap.entrySet()) {
            String topic = entry.getKey();
            ListenerWrapperMap wrapperMap = entry.getValue();

            String tagList = String.join("||", wrapperMap.keySet());
            subscription.put(topic, tagList);
        }

        consumer = new DefaultMQPushConsumer();
        consumer.setConsumerGroup(this.consumeGroup);
        consumer.setNamesrvAddr(this.namesrvAddr);
        consumer.setSubscription(subscription);
        consumer.setInstanceName(this.instanceName);
        consumer.setConsumeThreadMax(this.consumeThreadMax);
        consumer.setConsumeThreadMin(this.consumeThreadMin);
        consumer.setConsumeMessageBatchMaxSize(this.consumeMessageBatchMaxSize);
        consumer.setConsumeFromWhere(this.consumeFromWhere);
        consumer.setMessageModel(this.messageModel);
        consumer.registerMessageListener(new CommonMessageListenerConcurrently());

    }

    class CommonMessageListenerConcurrently implements MessageListenerConcurrently {

        public CommonMessageListenerConcurrently() {

        }

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

            for (MessageExt msg : msgs) {

                String topic = msg.getTopic();
                String tags = msg.getTags();
                String payloadStr = new String(msg.getBody(), StandardCharsets.UTF_8);
                log.info("ReceiveMqMsg topic={} tags={} msgId={} queueId={} queueOffset={}", topic, tags,
                        msg.getMsgId(), msg.getQueueId(), msg.getQueueOffset());

                LinkedHashSet<ListenerWrapper> wrappers = listenerWrapperMap.get(topic).get(tags);
                try {
                    for (ListenerWrapper wrapper : wrappers) {

                        Object payload = JSON.parseObject(payloadStr, wrapper.getBodyClazz());
                        GeneralMQListener<Object> generalMQListener = (GeneralMQListener<Object>) wrapper.getGeneralMQListener();
                        boolean result = generalMQListener.consume(msg, payload);

                        log.info("ConsumeMqMsg topic={} tags={} msgId={} queueId={} queueOffset={} result={}", topic, tags,
                                msg.getMsgId(), msg.getQueueId(), msg.getQueueOffset(), result);

                        return result? ConsumeConcurrentlyStatus.CONSUME_SUCCESS : ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                } catch (Throwable throwable) {
                    log.error("ConsumeMqMsg error topic={} tags={} msgId={} queueId={} queueOffset={} result={}", topic, tags,
                            msg.getMsgId(), msg.getQueueId(), msg.getQueueOffset(), throwable);
                    throw throwable;
                }

            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }


    public static class ListenerWrapperMap extends HashMap<String/* tag */, LinkedHashSet<ListenerWrapper>> {

    }

    @Data
    public static class ListenerWrapper {
        GeneralMQListener<?> generalMQListener;
        Class<?> bodyClazz;

        public ListenerWrapper(GeneralMQListener<?> generalMQListener, Class<?> bodyClazz) {
            this.generalMQListener = generalMQListener;
            this.bodyClazz = bodyClazz;
        }
    }


    @Override
    public void destroy() throws Exception {

    }
}
