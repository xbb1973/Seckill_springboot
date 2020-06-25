package slf.xbb.controller;

import com.alibaba.druid.util.StringUtils;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import slf.xbb.controller.view.OrderVo;
import slf.xbb.error.BussinessException;
import slf.xbb.error.EmBusinessError;
import slf.xbb.mq.MqProducer;
import slf.xbb.response.CommonReturnType;
import slf.xbb.service.ItemService;
import slf.xbb.service.OrderService;
import slf.xbb.service.PromoService;
import slf.xbb.service.model.OrderModel;
import slf.xbb.service.model.UserModel;
import sun.nio.ch.ThreadPool;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.*;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/4 7:45 上午
 * @description：订单控制器
 * @modifiedBy：
 * @version:
 */
@Slf4j
@Controller("order")
@RequestMapping(path = "/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoService promoService;

    private ExecutorService executorService;

    private RateLimiter orderRateLimiter;

    @PostConstruct
    public void init() {
        // 不推荐使用该方式创建线程池！！！！
        // 后续修改
        executorService = Executors.newFixedThreadPool(20);
        executorService =
                new ThreadPoolExecutor(
                        10,
                        20,
                        5,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(40),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy()
                        // new ThreadPoolExecutor.CallerRunsPolicy()
                        // new ThreadPoolExecutor.DiscardOldestPolicy()
                        // new ThreadPoolExecutor.DiscardPolicy()
                );

        orderRateLimiter = RateLimiter.create(100);
    }

    /**
     * 封装下单请求
     *
     * @param itemId
     * @param amount
     * @return
     */
    @RequestMapping(path = "/genseckilltoken", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType genSeckillToken(@RequestParam(name = "itemId") Integer itemId,
                                            @RequestParam(name = "promoId") Integer promoId) throws BussinessException {

        // 1、用户登陆态token实现
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BussinessException(EmBusinessError.USER_NOT_LOGIN);
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BussinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        // 2、调用promo服务生成秒杀令牌token
        String promoToken = promoService.genSeckillToken(promoId, itemId, userModel.getId());

        if (promoToken == null) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "令牌生成失败");
        }
        return CommonReturnType.create(promoToken);
    }

    /**
     * 封装下单请求
     *
     * @param itemId
     * @param amount
     * @return
     */
    @RequestMapping(path = "/create", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId,
                                        @RequestParam(name = "promoToken", required = false) String promoToken) throws BussinessException {
        // 限流
        // if (!orderRateLimiter.tryAcquire()) {
        //     throw new BussinessException(EmBusinessError.UNKNOWN_ERROR, "限流！！");
        // }

        // 1、登陆态session实现
        //获取用户的登陆信息
        // Boolean is_login = (Boolean) this.httpServletRequest.getSession().getAttribute("IS_LOGIN");
        // if (is_login == null || !is_login.booleanValue()) {
        //     throw new BussinessException(EmBusinessError.USER_NOT_LOGIN);
        // }
        // UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        // 2、登陆态token实现
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BussinessException(EmBusinessError.USER_NOT_LOGIN);
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BussinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //校验秒杀令牌是否正确
        if (promoId != null) {
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_" + promoId + "_userid_" + userModel.getId() + "_itemid_" + itemId);
            if (inRedisPromoToken == null) {
                throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
            if (!StringUtils.equals(promoToken, inRedisPromoToken)) {
                throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
        }

        // 判断售罄状态，利用redis，避免了使用库存流水来判断售罄
        // 将其放入秒杀令牌 大闸判断中
        // if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemId)) {
        //     throw new BussinessException(EmBusinessError.STOCK_INVALID);
        // }

        // 开启线程池，多个线程处理多个请求
        // 拥塞窗口为nThreads，用来队列化泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {
            /**
             * Computes a result, or throws an exception if unable to do so.
             *
             * @return computed result
             * @throws Exception if unable to compute a result
             */
            @Override
            public Object call() throws Exception {
                String itemStockLogId = itemService.initItemStockLog(itemId, amount);
                if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount, itemStockLogId)) {
                    throw new BussinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
                }

                return null;
            }
        });

        // 将这步耗时的操作放入线程池中
        // // 加入库存流水init状态
        // String itemStockLogId = itemService.initItemStockLog(itemId, amount);
        // // 异步下单
        // // OrderModel orderModel = null;
        // // orderModel = orderService.createOrder(userModel.getId(), itemId, amount);
        // // orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);
        // // return CommonReturnType.create(orderModel);
        // boolean hasTransactionAsyncReduceStock = mqProducer.transactionAsyncReduceStock(userModel.getId(), itemId, promoId, amount, itemStockLogId);

        try {
            future.get();
        } catch (InterruptedException e) {
            throw new BussinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
        } catch (ExecutionException e) {
            throw new BussinessException(EmBusinessError.UNKNOWN_ERROR, "下单失败");
        } finally {
        }
        return CommonReturnType.create(null);
    }

    private OrderVo convertFromOrderModelToOrderVo(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderVo orderVo = new OrderVo();
        BeanUtils.copyProperties(orderModel, orderVo);
        return orderVo;
    }
}
