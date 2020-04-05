package slf.xbb.service;

import slf.xbb.service.model.PromoModel;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/5 9:34 上午
 * @description：促销秒杀服务接口
 * @modifiedBy：
 * @version:
 */
public interface PromoService {

    /**
     * 通过itemId获取促销秒杀信息，才能对前端显示做修改
     * @param itemId
     * @return
     */
    PromoModel getPromoByItemId(Integer itemId);

}
