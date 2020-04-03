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
     */
    ItemModel createItem(ItemModel itemModel) throws BussinessException;

    /**
     * 商品列表浏览
     */
    List<ItemModel> listItem();

    /**
     * 商品详情浏览
     */
    ItemModel getItemById(Integer id);
}
