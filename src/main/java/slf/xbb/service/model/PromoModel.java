package slf.xbb.service.model;

import lombok.Data;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/5 9:22 上午
 * @description：促销秒杀活动模型
 * @modifiedBy：
 * @version:
 */
@Data
public class PromoModel {
    /**
     * 促销秒杀信息id
     */
    private Integer id;
    /**
     * 促销秒杀活动名称
     */
    private String promoName;
    /**
     * 开始时间
     */
    private DateTime startDate;
    /**
     * 截止时间
     */
    private DateTime endDate;
    /**
     * 促销秒杀物品id
     */
    private Integer itemId;
    /**
     * 促销秒杀价格
     */
    private BigDecimal promoItemPrice;
    /**
     * 秒杀活动状态
     * 0、无活动
     * 1、未开始
     * 2、进行
     * 3、结束
     */
    private Integer status;
}
