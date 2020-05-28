package slf.xbb.mq;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import slf.xbb.dao.ItemStockLogDoMapper;
import slf.xbb.domain.ItemStockLogDo;
import slf.xbb.error.BussinessException;
import slf.xbb.service.OrderService;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：xbb
 * @date ：Created in 2020/5/28 12:06 下午
 * @description：MQ生产者
 * @modifiedBy：
 * @version:
 */
@Component
public class MqProducer {

    private DefaultMQProducer defaultMQProducer;

    private TransactionMQProducer transactionMQProducer;

    @Value("${mq.nameserver.addr}")
    private String nameSrvAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemStockLogDoMapper itemStockLogDoMapper;

    @PostConstruct
    public void init() throws MQClientException {
        defaultMQProducer = new DefaultMQProducer("producer_group");
        defaultMQProducer.setNamesrvAddr(nameSrvAddr);
        defaultMQProducer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");
        transactionMQProducer.setNamesrvAddr(nameSrvAddr);
        transactionMQProducer.start();
        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                // create order
                HashMap<String, Object> argsMap = (HashMap<String, Object>) o;
                Integer userId = (Integer) argsMap.get("userId");
                Integer itemId = (Integer) argsMap.get("itemId");
                Integer promoId = (Integer) argsMap.get("promoId");
                Integer amount = (Integer) argsMap.get("amount");
                String itemStockLogId = (String) argsMap.get("itemStockLogId");
                try {
                    orderService.createOrder(userId, itemId, promoId, amount, itemStockLogId);
                } catch (BussinessException e) {
                    e.printStackTrace();
                    // 设置对应的itemStockLog为回滚状态
                    ItemStockLogDo itemStockLogDo = itemStockLogDoMapper.selectByPrimaryKey(itemStockLogId);
                    if (itemStockLogDo == null) {
                        // to do
                    }
                    itemStockLogDo.setStatus(3);
                    itemStockLogDoMapper.updateByPrimaryKeySelective(itemStockLogDo);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                } finally {
                }
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                // 当LocalTransactionState.UNKNOW状态时，会定期执行该方法
                // 根据数据库是否扣减来发送Redis库存扣减MQ
                System.out.println(messageExt.toString());
                String jsonString = new String(messageExt.getBody());
                Map<String, Object> bodyMap = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) bodyMap.get("itemId");
                Integer amount = (Integer) bodyMap.get("amount");
                String itemStockLogId = (String) bodyMap.get("itemStockLogId");
                ItemStockLogDo itemStockLogDo = itemStockLogDoMapper.selectByPrimaryKey(itemStockLogId);
                if (itemStockLogDo == null) {
                    return LocalTransactionState.UNKNOW;
                }
                if (itemStockLogDo.getStatus().intValue() == 2) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                } else if (itemStockLogDo.getStatus().intValue() == 1) {
                    return LocalTransactionState.UNKNOW;
                } else {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }
            }
        });
    }

    public boolean transactionAsyncReduceStock(Integer userId, Integer itemId, Integer promoId, Integer amount, String itemStockLogId) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        bodyMap.put("itemStockLogId", itemStockLogId);

        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("userId", userId);
        argsMap.put("itemId", itemId);
        argsMap.put("promoId", promoId);
        argsMap.put("amount", amount);
        argsMap.put("itemStockLogId", itemStockLogId);

        Message message = new Message(topicName, "Tags_Stock", "Keys_StockIncrease",
                JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        TransactionSendResult transactionSendResult = new TransactionSendResult();
        try {
            transactionSendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);
            System.out.println(transactionSendResult);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } finally {
        }
        if (transactionSendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE) {
            return true;
        } else if (transactionSendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE) {
            return false;
        } else {
            return false;
        }
    }

    public boolean asyncReduceStock(Integer itemId, Integer amount) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("itemId", itemId);
        bodyMap.put("amount", amount);
        Message message = new Message(topicName, "TagA", "increase",
                JSON.toJSON(bodyMap).toString().getBytes(Charset.forName("UTF-8")));
        try {
            SendResult sendResult = defaultMQProducer.send(message);
            System.out.println(sendResult);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
        }
        return true;
    }
}
