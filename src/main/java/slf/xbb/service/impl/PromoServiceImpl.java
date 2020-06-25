package slf.xbb.service.impl;

import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import slf.xbb.dao.ItemStockLogDoMapper;
import slf.xbb.dao.OrderDoMapper;
import slf.xbb.dao.PromoDoMapper;
import slf.xbb.dao.SequenceDoMapper;
import slf.xbb.domain.PromoDo;
import slf.xbb.error.BussinessException;
import slf.xbb.error.EmBusinessError;
import slf.xbb.service.ItemService;
import slf.xbb.service.PromoService;
import slf.xbb.service.UserService;
import slf.xbb.service.model.ItemModel;
import slf.xbb.service.model.PromoModel;
import slf.xbb.service.model.UserModel;

import javax.xml.crypto.Data;
import java.sql.DataTruncation;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/5 9:33 上午
 * @description：促销秒杀服务实现
 * @modifiedBy：
 * @version:
 */
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDoMapper promoDoMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDoMapper orderDoMapper;

    @Autowired
    private ItemStockLogDoMapper itemStockLogDoMapper;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 通过itemId获取促销秒杀信息，才能对前端显示做修改
     *
     * @param itemId
     * @return
     */
    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDo promoDo = promoDoMapper.selectByItemId(itemId);
        PromoModel promoModel = covertFromPromoDoToPromoModel(promoDo);
        if (promoModel == null) {
            return null;
        }

        // 判断当前时间，秒杀活动是否即将开始或开始进行
        int status = NowDateCompareToPromoDate(promoModel.getStartDate(), promoModel.getEndDate());
        promoModel.setStatus(status);
        return promoModel;
    }

    /**
     * @param promoId
     * @Description: 活动发布时同步库存到缓存，为优化串行库存操作做准备
     * @Param:
     * @return:
     * @Date: 2020/5/28
     * @Author: xbb1973
     */
    @Override
    public void publishPromo(Integer promoId) {
        // 通过活动id获得活动
        PromoDo promoDo = promoDoMapper.selectByItemId(promoId);
        if (promoDo == null || promoDo.getItemId() == null || promoDo.getItemId().intValue() == 0) {
            return;
        }
        PromoModel promoModel = covertFromPromoDoToPromoModel(promoDo);
        if (promoModel == null) {
            return;
        }
        // 此时商品可能被售卖，此时需要额外添加上下架操作
        // 假设库存不会发生变化进行处理
        String key = "promo_item_stock_" + promoDo.getItemId();
        ItemModel itemModel = itemService.getItemByIdInCache(promoDo.getItemId());
        // 将库存同步到redis内
        redisTemplate.opsForValue().set(key, itemModel.getStock());
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
        // 此时应该由运营后台来调用该方法，发布promo
        // 这里不考虑，直接在ItemController发布

        // 设置令牌大闸的流量限制
        redisTemplate.opsForValue().set("promo_door_count_" + promoId, itemModel.getStock() * 2);
        redisTemplate.expire("promo_door_count_" + promoId, 5, TimeUnit.MINUTES);
    }

    /**
     * 返回秒杀活动状态
     * 1、未开始
     * 2、进行
     * 3、结束
     *
     * @param start
     * @param end
     * @return
     */
    private int NowDateCompareToPromoDate(DateTime start, DateTime end) {
        if (start.isAfterNow()) {
            return 1;
        } else if (end.isBeforeNow()) {
            return 3;
        } else {
            return 2;
        }
    }

    /**
     * @param promoId
     * @Description:
     * @Param:
     * @return:
     * @Date: 2020/6/24
     * @Author: xbb1973
     */
    @Override
    public String genSeckillToken(Integer promoId, Integer itemId, Integer userId) {

        // 0、判断售罄状态，利用redis，避免了使用库存流水来判断售罄
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            return null;
        }

        // 1、判断redis中的库存状态是否可用
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
            return null;
        } else {
            // 2、根据时间判断聚合status字段，判断promo活动是否可用
            PromoDo promoDO = promoDoMapper.selectByPrimaryKey(promoId);
            PromoModel promoModel = covertFromPromoDoToPromoModel(promoDO);
            if (promoModel == null) {
                return null;
            } else {
                if (promoModel.getStartDate().isAfterNow()) {
                    promoModel.setStatus(1);
                } else if (promoModel.getEndDate().isBeforeNow()) {
                    promoModel.setStatus(3);
                } else {
                    promoModel.setStatus(2);
                }

                // status == 2表示进行中
                if (promoModel.getStatus() != 2) {
                    return null;
                } else {
                    // 3、当前商品的promo活动进行中，使用缓存校验
                    ItemModel itemModel = itemService.getItemByIdInCache(itemId);
                    if (itemModel == null) {
                        return null;
                    } else {
                        UserModel userModel = userService.getUserByIdInCache(userId);
                        if (userModel == null) {
                            return null;
                        } else {
                            long result = redisTemplate.opsForValue().increment("promo_door_count_" + promoId, -1L);
                            if (result < 0L) {
                                return null;
                            } else {
                                String token = UUID.randomUUID().toString().replace("-", "");
                                redisTemplate.opsForValue().set("promo_token_" + promoId + "_userid_" + userId + "_itemid_" + itemId, token);
                                redisTemplate.expire("promo_token_" + promoId + "_userid_" + userId + "_itemid_" + itemId, 5L, TimeUnit.MINUTES);
                                return token;
                            }
                        }
                    }
                }
            }
        }
    }

    private PromoModel covertFromPromoDoToPromoModel(PromoDo promoDo) {
        if (promoDo == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDo, promoModel);
        promoModel.setStartDate(new DateTime(promoDo.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDo.getEndDate()));
        return promoModel;
    }
}
