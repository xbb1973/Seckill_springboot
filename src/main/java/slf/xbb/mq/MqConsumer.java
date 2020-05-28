package slf.xbb.mq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import slf.xbb.dao.ItemStockDoMapper;


import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/28 12:07 下午
 * @description：MQ消费者
 * @modifiedBy：
 * @version:
 */
@Component
public class MqConsumer {

    private DefaultMQPushConsumer pushConsumer;

    @Value("${mq.nameserver.addr}")
    private String nameSrvAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private ItemStockDoMapper itemStockDoMapper;

    @PostConstruct
    public void init() throws MQClientException {
        pushConsumer = new DefaultMQPushConsumer("stock_consumer_group");
        pushConsumer.setNamesrvAddr(nameSrvAddr);
        pushConsumer.subscribe(topicName, "*");
        pushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                MessageExt messageExt = list.get(0);
                System.out.println(messageExt.toString());
                String jsonString = new String(messageExt.getBody());
                Map<String, Object> bodyMap = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) bodyMap.get("itemId");
                Integer amount = (Integer) bodyMap.get("amount");
                int affectRows = itemStockDoMapper.decreaseStock(itemId, amount);
                if (affectRows > 0) {
                    // 更新库存成功
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } else {
                    // 更新库存失败
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        });
        pushConsumer.start();
    }
}
