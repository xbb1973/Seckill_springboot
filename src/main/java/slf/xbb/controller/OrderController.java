package slf.xbb.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import slf.xbb.controller.view.OrderVo;
import slf.xbb.error.BussinessException;
import slf.xbb.error.EmBusinessError;
import slf.xbb.response.CommonReturnType;
import slf.xbb.service.OrderService;
import slf.xbb.service.model.OrderModel;
import slf.xbb.service.model.UserModel;

import javax.servlet.http.HttpServletRequest;

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
                                        @RequestParam(name = "promoId") Integer promoId) throws BussinessException {
        //获取用户的登陆信息
        Boolean is_login = (Boolean) this.httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (is_login == null || !is_login.booleanValue()) {
            throw new BussinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        OrderModel orderModel = null;
        // orderModel = orderService.createOrder(userModel.getId(), itemId, amount);
        orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);
        return CommonReturnType.create(orderModel);
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
