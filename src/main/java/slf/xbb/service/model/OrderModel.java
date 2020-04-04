package slf.xbb.service.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/4 7:10 上午
 * @description：交易领域模型
 * @modifiedBy：
 * @version:
 */
@Data
public class OrderModel {

    /**
     * 交易id 带有明显的交易信息，2020040400012345
     */
    private String id;

    /**
     * 购买用户id
     */
    private Integer userId;

    /**
     * 购买商品id
     */
    private Integer itemId;

    /**
     * 购买商品的单价，记录下单时的商品价格，
     * 因为商品价格会变动，需要冗余一个属性来保存当时的下单单价
     */
    private BigDecimal itemPrice;

    /**
     * 购买数量
     */
    private Integer amount;

    /**
     * 购买金额
     */
    private BigDecimal orderPrice;

}
