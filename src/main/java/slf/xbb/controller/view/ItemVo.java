package slf.xbb.controller.view;

import lombok.Data;
import org.joda.time.DateTime;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/3 8:28 下午
 * @description：ItemVo提供给前端的视图模型
 * @modifiedBy：
 * @version:
 */
@Data
public class ItemVo {

    private Integer id;

    /**
     * 商品名称
     */
    // @NotBlank(message = "商品名称不能为空")
    private String title;

    /**
     * 商品价格
     */
    // @Digits(message = "商品价格必须合法")
    // @Min(value = 0, message = "商品价格必须大于0")
    private BigDecimal price;

    /**
     * 商品库存
     */
    // @NotNull(message = "商品库存不能不填")
    private Integer stock;

    /**
     * 商品描述
     */
    // @NotBlank(message = "商品描述不能为空")
    private String description;

    /**
     * 商品销量
     */
    private Integer sales;

    /**
     * 商品图片的url
     */
    // @NotBlank(message = "图片信息不能为空")
    private String imgUrl;

    /**
     * 聚合PromoVo
     */
    // private PromoVo promoVo;


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
    private String promoStartDate;

    private String promoEndDate;

}
