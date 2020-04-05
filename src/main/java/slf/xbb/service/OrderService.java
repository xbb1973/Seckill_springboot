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
     * 秒杀方案选择：
     * 1、（通过url判断活动，推荐使用）通过前端url上传过来秒杀活动id，然后下单接口内校验对应id是否属于对应商品且活动已开始
     * 2、（在接口内判断活动，不做活动对也要判断）直接在下单接口内判断对应商品是否存在秒杀活动，若存在进行中则已秒杀价格下单
     * @param userId
     * @param itemId
     * @param amount
     * @return
     */
    OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BussinessException;


    /**
     * 创建订单，对应的请求体需要参数为：用户id 商品id 购买数量
     * 秒杀方案选择：
     * 1、（通过url判断活动，推荐使用）通过前端url上传过来秒杀活动id，然后下单接口内校验对应id是否属于对应商品且活动已开始
     * 2、（在接口内判断活动，不做活动对也要判断）直接在下单接口内判断对应商品是否存在秒杀活动，若存在进行中则已秒杀价格下单
     * @param userId
     * @param itemId
     * @param promoId
     * @param amount
     * @return
     */
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BussinessException;



}
