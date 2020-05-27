package slf.xbb.service;

import slf.xbb.error.BussinessException;
import slf.xbb.service.model.ItemModel;
import slf.xbb.service.model.UserModel;

import java.util.List;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 2:35 上午
 * @description：User service
 * @modifiedBy：
 * @version:
 */
public interface ItemService {

    /**
     * 创建商品
     * @param itemModel
     * @return
     * @throws BussinessException
     */
    ItemModel createItem(ItemModel itemModel) throws BussinessException;

    /**
     * 商品列表浏览
     * @return
     */
    List<ItemModel> listItem();

    /**
     * 获取ItemModel商品信息
     * 通过访问数据库聚合
     * @param id
     * @return
     */
    ItemModel getItemById(Integer id);

    /**
     * 获取itemModel 内聚了 promoModel
     * 通过缓存访问
     * @param id
     * @return
     */
    ItemModel getItemByIdInCache(Integer id);

    /**
     * 削减库存
     * @param itemId
     * @param amount
     * @return
     */
    boolean decreaseStock(Integer itemId, Integer amount);

    /**
     * 商品销量增加，意味着库存削减成功，下单成功
     * @param itemId
     * @param amount
     */
    void increseSales(Integer itemId, Integer amount);
}
