package slf.xbb.controller.view;

import lombok.Data;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/5 9:35 上午
 * @description：促销秒杀前端视图
 * @modifiedBy：
 * @version:
 */
@Data
public class PromoVo {
    /**
     * 0、没有活动
     * 1、活动待开始
     * 2、活动进行中
     * 3、活动结束
     */
    private Integer promoStatus;

    /**
     * 活动优惠价格
     */
    private BigDecimal promoPrice;

    /**
     * 活动id，之后的交易行为需要活动id
     */
    private Integer promoId;

    /**
     * 活动开始和截止时间，用于做倒计时展示
     */
    private DateTime startData;

    private DateTime endData;


}
