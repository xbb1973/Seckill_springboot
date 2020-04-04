package slf.xbb.service;

import slf.xbb.error.BussinessException;
import slf.xbb.service.model.OrderModel;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/4 7:37 上午
 * @description：订单服务
 * @modifiedBy：
 * @version:
 */
public interface OrderService {
    /**
     * 创建订单，对应的请求体需要参数为：用户id 商品id 购买数量
     * @param userId
     * @param itemId
     * @param amount
     * @return
     */
    OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BussinessException;



}
