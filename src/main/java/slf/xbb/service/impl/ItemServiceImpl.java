package slf.xbb.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import slf.xbb.dao.ItemDoMapper;
import slf.xbb.dao.ItemStockDoMapper;
import slf.xbb.dao.ItemStockLogDoMapper;
import slf.xbb.domain.ItemDo;
import slf.xbb.domain.ItemStockDo;
import slf.xbb.domain.ItemStockLogDo;
import slf.xbb.error.BussinessException;
import slf.xbb.error.EmBusinessError;
import slf.xbb.mq.MqConsumer;
import slf.xbb.mq.MqProducer;
import slf.xbb.service.ItemService;
import slf.xbb.service.PromoService;
import slf.xbb.service.model.ItemModel;
import slf.xbb.service.model.PromoModel;
import slf.xbb.validator.ValidationResult;
import slf.xbb.validator.ValidatorImpl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/3 7:24 下午
 * @description：ItemService接口实现
 * @modifiedBy：
 * @version:
 */
@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDoMapper itemDoMapper;

    @Autowired
    private ItemStockDoMapper itemStockDoMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemStockLogDoMapper itemStockLogDoMapper;

    /**
     * 创建商品
     *
     * @param itemModel
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemModel createItem(ItemModel itemModel) throws BussinessException {
        // 校验参数
        ValidationResult result = validator.validationResult(itemModel);
        if (result.isHasErrors()) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        // convert itemModel to dataObject
        ItemDo itemDo = convertFromItemModelToItemDo(itemModel);
        // 写入数据库
        itemDoMapper.insertSelective(itemDo);
        // 返回创建完成的对象
        itemModel.setId(itemDo.getId());

        // 库存Do做同样的操作
        ItemStockDo itemStockDo = convertFromItemModelToItemStockDo(itemModel);
        itemStockDoMapper.insertSelective(itemStockDo);

        return getItemById(itemModel.getId());
    }

    /**
     * 商品列表浏览
     */
    @Override
    public List<ItemModel> listItem() {
        List<ItemDo> itemDoList = itemDoMapper.listItem();
        List<ItemModel> itemModelList = itemDoList.stream().map(itemDo -> {
            ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(itemDo.getId());
            ItemModel itemModel = convertToItemModel(itemDo, itemStockDo);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    /**
     * 商品详情浏览
     *
     * @param id
     * @return itemModel
     */
    @Override
    public ItemModel getItemById(Integer id) {
        // 操作数据库获取item
        ItemDo itemDo = itemDoMapper.selectByPrimaryKey(id);
        if (itemDo == null) {
            return null;
        }
        // 操作数据库获取itemStock
        ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(itemDo.getId());

        // 聚合成itemModel
        ItemModel itemModel = convertToItemModel(itemDo, itemStockDo);

        // 获取item promo信息，聚合到item model中
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    /**
     * 获取itemModel 内聚了 promoModel
     * 通过缓存访问
     *
     * @param id
     * @return itemModel
     */
    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        String key = "item_validate_" + id;
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get(key);
        if (itemModel == null) {
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set(key, itemModel);
            redisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }
        return itemModel;
    }


    /**
     * 削减库存，
     *
     * @param itemId
     * @param amount
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Integer itemId, Integer amount) {
        // OptimizeImpl:修改库存缓存化
        String key = "promo_item_stock_" + itemId;
        // 使用redis increment进行库存扣除
        long restStock = redisTemplate.opsForValue().increment(key, amount.intValue() * -1);
        // 库存至多减少至0
        if (restStock >= 0) {
            // 更新缓存成功，发送MQ消息
            // 将发送异步消息、削减库存的操作分离，规避Redis无法回补库存的问题
            // boolean decreaseStockSuccess = mqProducer.asyncReduceStock(itemId, amount.intValue());
            // if (!decreaseStockSuccess) {
            //     // 发送MQ消息失败，更新缓存失败
            //     redisTemplate.opsForValue().increment(key, amount.intValue());
            //     return false;
            // }
            if (restStock == 0) {
                // 打上售罄标识
                redisTemplate.opsForValue().set("promo_item_stock_invalid_" + itemId, "true");
            }
            return true;
        } else {
            // 更新库存失败
            redisTemplate.opsForValue().increment(key, amount.intValue());
            return false;
        }

        // OriginalImpl:直接在数据库中进行修改
        // affectRows = 0时是where条件判断失败
        // affectRows = 1时更新库存成功
        // int affectRows = itemStockDoMapper.decreaseStock(itemId, amount);
        // if (affectRows > 0) {
        //     // 更新库存成功
        //     return true;
        // } else {
        //     // 更新库存失败
        //     return false;
        // }
    }


    /**
     * 异步削减库存
     *
     * @param itemId
     * @param amount
     * @return
     */
    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        boolean decreaseStockSuccess = mqProducer.asyncReduceStock(itemId, amount.intValue());
        return decreaseStockSuccess;
    }

    @Override
    public boolean increaseRedisStock(Integer itemId, Integer amount) {
        String key = "promo_item_stock_" + itemId;
        long restStock = redisTemplate.opsForValue().increment(key, amount.intValue());
        return true;
    }

    @Override
    public boolean asyncIncreaseStock(Integer itemId, Integer amount) {
        return false;
    }


    /**
     * 商品销量增加，意味着库存削减成功，下单成功
     *
     * @param itemId
     * @param amount
     */
    @Override
    @Transactional
    public void increseSales(Integer itemId, Integer amount) {
        itemDoMapper.increseSales(itemId, amount);
    }

    /**
     * @Description:下单前初始化库存流水
     * @Param: [itemId, amount]
     * @return: void
     * @Date: 2020/5/28
     * @Author: xbb1973
     */
    @Override
    @Transactional
    public String initItemStockLog(Integer itemId, Integer amount) {
        ItemStockLogDo itemStockLogDo = new ItemStockLogDo();
        itemStockLogDo.setItemId(itemId);
        itemStockLogDo.setAmount(amount);
        itemStockLogDo.setStockLogId(UUID.randomUUID().toString().replace("-", ""));
        itemStockLogDo.setStatus(1);
        itemStockLogDoMapper.insertSelective(itemStockLogDo);
        return itemStockLogDo.getStockLogId();
    }

    private ItemDo convertFromItemModelToItemDo(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDo itemDo = new ItemDo();
        BeanUtils.copyProperties(itemModel, itemDo);
        return itemDo;
    }

    private ItemStockDo convertFromItemModelToItemStockDo(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDo itemStockDo = new ItemStockDo();
        itemStockDo.setStock(itemModel.getStock());
        itemStockDo.setItemId(itemModel.getId());
        return itemStockDo;
    }

    private ItemModel convertToItemModel(ItemDo itemDo, ItemStockDo itemStockDo) {
        if (itemDo == null) {
            return null;
        }
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDo, itemModel);
        itemModel.setStock(itemStockDo.getStock());
        return itemModel;
    }
}
