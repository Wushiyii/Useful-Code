package com.wushiyii;

import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
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
    private Map<String, ListenerWrapperMap> listenerWrapperMap = new HashMap<>();

    /* MQ internal properties */
    private String consumeGroup;
    private String namesrvAddr;
    private String instanceName;
    private ConsumeFromWhere consumeFromWhere;
    private MessageModel messageModel;
    private Integer consumeThreadMin;
    private Integer consumeThreadMax;
    private Integer messageBatchMaxSize;

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
        this.messageBatchMaxSize = Objects.nonNull(mqProperties.getMessageBatchMaxSize()) ? mqProperties.getMessageBatchMaxSize() : DEFAULT_BATCH_SIZE;
    }


    public void init(ApplicationContext applicationContext) {

        String[] beanNames = applicationContext.getBeanNamesForAnnotation(MQListener.class);
        for (String beanName : beanNames) {
            GeneralMQListener<?> matchedMQListener = applicationContext.getBean(beanName, GeneralMQListener.class);
            Validate.notNull(matchedMQListener, "The class " + beanName+ ", annotation with @MQListener, doesn't implements with GeneralMQListener");

            MQListener mqListener = AnnotationUtils.findAnnotation(matchedMQListener.getClass(), MQListener.class);
            validateAndAdd(mqListener, matchedMQListener);
        }



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

    public static class ListenerWrapperMap extends HashMap<String, LinkedHashSet<ListenerWrapper>> {

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
