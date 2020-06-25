package slf.xbb.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import slf.xbb.dao.ItemStockLogDoMapper;
import slf.xbb.dao.OrderDoMapper;
import slf.xbb.dao.SequenceDoMapper;
import slf.xbb.domain.ItemStockLogDo;
import slf.xbb.domain.OrderDo;
import slf.xbb.domain.SequenceDo;
import slf.xbb.error.BussinessException;
import slf.xbb.error.EmBusinessError;
import slf.xbb.service.ItemService;
import slf.xbb.service.OrderService;
import slf.xbb.service.UserService;
import slf.xbb.service.model.ItemModel;
import slf.xbb.service.model.OrderModel;
import slf.xbb.service.model.UserModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/4 7:41 上午
 * @description：订单服务实现
 * @modifiedBy：
 * @version:
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDoMapper orderDoMapper;

    @Autowired
    private ItemStockLogDoMapper itemStockLogDoMapper;

    @Autowired
    private SequenceDoMapper sequenceDoMapper;

    /**
     * 创建订单，对应的请求体需要参数为：用户id 商品id 购买数量
     *
     * @param userId
     * @param itemId
     * @param amount
     * @return
     */
    @Transactional
    @Override
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BussinessException {
        // 1、检验下单状态，下单的商品是否存在，用户是否合法，购买数量是否正确
        UserModel userModel = userService.getUserById(userId);
        ItemModel itemModel = itemService.getItemById(itemId);
        if (userModel == null || itemModel == null || (amount <= 0 || amount > 99)) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品/用户信息/购买数量：参数异常");
        }

        // 2、落单减库存 / 支付减库存（支付减库存会出现超卖的问题）
        boolean hasDecreaseStock = itemService.decreaseStock(itemId, amount);
        if (!hasDecreaseStock) {
            throw new BussinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        // 3、订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);
        orderModel.setItemPrice(itemModel.getPrice());
        orderModel.setOrderPrice(itemModel.getPrice().multiply(new BigDecimal(amount)));

        // 生成交易流水号，这里的order没有定义自增id
        OrderDo orderDo = new OrderDo();
        try {
            orderModel.setId(generateOrderNo());
            orderDo = convertFromOrderModelToOrderDo(orderModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        orderDoMapper.insertSelective(orderDo);
        // 加上商品销量
        itemService.increseSales(itemId, amount);
        // 4、返回前端

        return orderModel;
    }

    /**
     * 创建订单，对应的请求体需要参数为：用户id 商品id 购买数量
     * 秒杀方案选择：
     * 1、（通过url判断活动，推荐使用）通过前端url上传过来秒杀活动id，然后下单接口内校验对应id是否属于对应商品且活动已开始
     * 2、（在接口内判断活动，不做活动对也要判断）直接在下单接口内判断对应商品是否存在秒杀活动，若存在进行中则已秒杀价格下单
     *
     * @param userId
     * @param itemId
     * @param promoId
     * @param amount
     * @param itemStockLogId
     * @return
     */
    @Transactional
    @Override
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String itemStockLogId) throws BussinessException {

        // 1、(校验商品、用户、活动信息等)检验下单状态，下单的商品是否存在，用户是否合法，购买数量是否正确
        // 访问2次db
        // UserModel userModel = userService.getUserById(userId);
        // 访问3次db
        // ItemModel itemModel = itemService.getItemById(itemId);
        // 将userModel、itemModel转化为缓存模型

        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        UserModel userModel = userService.getUserByIdInCache(userId);

        // 校验
        // 此处由Token代为校验item和user合法性，不需要下单接口再校验一次
        // if (userModel == null || itemModel == null || (amount <= 0 || amount > 99)) {
        //     throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品/用户信息/购买数量：参数异常");
        // }
        if ((amount <= 0 || amount > 99)) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品/用户信息/购买数量：参数异常");
        }

        // 由token代替校验，下单接口不必再校验
        // 校验活动信息
        // if (promoId != null) {
        //     // 校验活动是否存在这个使用商品
        //     // 校验活动是否进行中
        //     //
        //     if (promoId.intValue() != itemModel.getPromoModel().getId()) {
        //         throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
        //     } else if (itemModel.getPromoModel().getStatus() != 2) {
        //         throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动还未开始");
        //     }
        // }

        // 2、落单减库存 / 支付减库存（支付减库存会出现超卖的问题）
        // 热点操作，Redis缓存
        // <update id="decreaseStock">
        //         update item_stock
        //         set stock = stock -  #{amount,jdbcType=INTEGER}
        //         where item_id = #{itemId,jdbcType=INTEGER}   #此处对item_id有行锁
        //             and stock >= #{amount,jdbcType=INTEGER}
        //     </update>
        boolean hasDecreaseStock = itemService.decreaseStock(itemId, amount);
        if (!hasDecreaseStock) {
            throw new BussinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        // 3、订单入库
        // 数据封装填充
        OrderModel orderModel = new OrderModel();
        orderModel.setItemId(itemId);
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);
        if (promoId == null) {
            orderModel.setItemPrice(itemModel.getPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        orderModel.setPromoId(promoId);

        // 生成交易流水号，这里的order没有定义自增id
        OrderDo orderDo = new OrderDo();
        try {
            orderModel.setId(generateOrderNo());
            orderDo = convertFromOrderModelToOrderDo(orderModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        orderDoMapper.insertSelective(orderDo);

        // 加上商品销量
        itemService.increseSales(itemId, amount);

        // 设置库存流水状态为成功
        // 这个操作看来没有提升性能，
        // 但实际上这个流水的操作针对的是itemStockLogId的行锁
        // 对不同的行锁操作，没有竞争、等待的压力，
        // 目前的压力瓶颈是item_id的行锁。
        ItemStockLogDo itemStockLogDo = itemStockLogDoMapper.selectByPrimaryKey(itemStockLogId);
        if (itemStockLogDo == null) {
            throw new BussinessException(EmBusinessError.UNKNOWN_ERROR);
        }
        itemStockLogDo.setStatus(2);
        itemStockLogDoMapper.updateByPrimaryKeySelective(itemStockLogDo);

        // 解决事务执行没问题，最后commit失败导致无法回滚Redis库存
        // TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        //     @Override
        //     public void afterCommit() {
        //         // 异步更新库存，规避无法回补Redis库存的问题
        //         boolean mqDecStockResult = itemService.asyncDecreaseStock(itemId, amount);
        //         // 问题：此处的消息发送失败无法处理
        //         // if (!mqDecStockResult) {
        //         //     itemService.increaseRedisStock(itemId, amount);
        //         //     throw new BussinessException(EmBusinessError.MQ_STOCK_FAIL);
        //         // }
        //     }
        // });

        // 异步更新库存，规避无法回补Redis库存的问题
        // 问题：transaction事务最后commit失败时，上述方案仍旧无效，
        // boolean mqDecStockResult = itemService.asyncDecreaseStock(itemId, amount);
        // if (!mqDecStockResult) {
        //     itemService.increaseRedisStock(itemId, amount);
        //     throw new BussinessException(EmBusinessError.MQ_STOCK_FAIL);
        // }

        // 4、返回前端
        return orderModel;
    }

    /**
     * 订单号有16位：
     * 1、前8位为时间信息，年月日   （当数据太多以时间纬度归档）
     * 2、中间6位为自增序列          （保证订单号不重复）
     * 3、最后2位为分库分表位         （）
     *
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String generateOrderNo() {
        // 订单号有16位：
        StringBuilder stringBuilder = new StringBuilder();
        // 1、前8位为时间信息，年月日   （当数据太多以时间纬度归档）
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);
        // 2、中间6位为自增序列          （保证订单号不重复）
        // mysql和orcal不同，因此建立sequence_info表来实现自增序列
        // 需要实现sequence_info的MBG
        int sequence = 0;
        // sequence要保证唯一，因此需要使用行级锁
        SequenceDo sequenceDo = sequenceDoMapper.getSequenceByName("order_info");
        sequence = sequenceDo.getCurrentValue();
        sequenceDo.setCurrentValue(sequenceDo.getCurrentValue() + sequenceDo.getStep());
        sequenceDoMapper.updateByPrimaryKeySelective(sequenceDo);

        stringBuilder.append(String.format("%06d", sequence));

        // 3、最后2位为分库分表位         （）
        // 暂时写死
        stringBuilder.append("00");
        // Integer userId = 1000122;
        // 分库分表位 = userId % 100;
        return stringBuilder.toString();
    }


    private OrderDo convertFromOrderModelToOrderDo(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDo orderDo = new OrderDo();
        BeanUtils.copyProperties(orderModel, orderDo);
        return orderDo;
    }

}
