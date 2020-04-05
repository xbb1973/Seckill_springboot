package slf.xbb.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import slf.xbb.dao.ItemDoMapper;
import slf.xbb.dao.ItemStockDoMapper;
import slf.xbb.dao.PromoDoMapper;
import slf.xbb.domain.ItemDo;
import slf.xbb.domain.ItemStockDo;
import slf.xbb.error.BussinessException;
import slf.xbb.error.EmBusinessError;
import slf.xbb.service.ItemService;
import slf.xbb.service.PromoService;
import slf.xbb.service.model.ItemModel;
import slf.xbb.service.model.PromoModel;
import slf.xbb.validator.ValidationResult;
import slf.xbb.validator.ValidatorImpl;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/3 7:24 下午
 * @description：ItemService接口实现
 * @modifiedBy：
 * @version:
 */
@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDoMapper itemDoMapper;

    @Autowired
    private ItemStockDoMapper itemStockDoMapper;

    @Autowired
    private PromoService promoService;

    /**
     * 创建商品
     *
     * @param itemModel
     */
    @Override
    @Transactional(rollbackFor = BussinessException.class)
    public ItemModel createItem(ItemModel itemModel) throws BussinessException {
        // 校验参数
        ValidationResult result = validator.validationResult(itemModel);
        if (result.isHasErrors()) {
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }
        // convert itemModel to dataObject
        ItemDo itemDo = convertFromItemModelToItemDo(itemModel);
        // 写入数据库
        itemDoMapper.insertSelective(itemDo);
        // 返回创建完成的对象
        itemModel.setId(itemDo.getId());

        // 库存Do做同样的操作
        ItemStockDo itemStockDo = convertFromItemModelToItemStockDo(itemModel);
        itemStockDoMapper.insertSelective(itemStockDo);

        return getItemById(itemModel.getId());
    }

    /**
     * 商品列表浏览
     */
    @Override
    public List<ItemModel> listItem() {
        List<ItemDo> itemDoList = itemDoMapper.listItem();
        List<ItemModel> itemModelList = itemDoList.stream().map(itemDo -> {
            ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(itemDo.getId());
            ItemModel itemModel = convertToItemModel(itemDo, itemStockDo);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    /**
     * 商品详情浏览
     *
     * @param id
     */
    @Override
    public ItemModel getItemById(Integer id) {
        // 操作数据库获取item
        ItemDo itemDo = itemDoMapper.selectByPrimaryKey(id);
        if (itemDo == null) {
            return null;
        }
        // 操作数据库获取itemStock
        ItemStockDo itemStockDo = itemStockDoMapper.selectByItemId(itemDo.getId());

        // 聚合成itemModel
        ItemModel itemModel = convertToItemModel(itemDo, itemStockDo);

        // 获取item promo信息，聚合到item model中
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    /**
     * 削减库存
     *
     * @param itemId
     * @param amount
     * @return
     */
    @Override
    @Transactional(rollbackFor = BussinessException.class)
    public boolean decreaseStock(Integer itemId, Integer amount) {
        // affectRows = 0时是where条件判断失败
        // affectRows = 1时更新库存成功
        int affectRows = itemStockDoMapper.decreaseStock(itemId, amount);
        if (affectRows > 0) {
            // 更新库存成功
            return true;
        } else {
            // 更新库存失败
            return false;
        }
    }

    /**
     * 商品销量增加，意味着库存削减成功，下单成功
     *
     * @param itemId
     * @param amount
     */
    @Override
    @Transactional
    public void increseSales(Integer itemId, Integer amount) {
        itemDoMapper.increseSales(itemId, amount);

    }

    private ItemDo convertFromItemModelToItemDo(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDo itemDo = new ItemDo();
        BeanUtils.copyProperties(itemModel, itemDo);
        return itemDo;
    }

    private ItemStockDo convertFromItemModelToItemStockDo(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDo itemStockDo = new ItemStockDo();
        itemStockDo.setStock(itemModel.getStock());
        itemStockDo.setItemId(itemModel.getId());
        return itemStockDo;
    }

    private ItemModel convertToItemModel(ItemDo itemDo, ItemStockDo itemStockDo) {
        if (itemDo == null) {
            return null;
        }
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDo, itemModel);
        itemModel.setStock(itemStockDo.getStock());
        return itemModel;
    }
}
