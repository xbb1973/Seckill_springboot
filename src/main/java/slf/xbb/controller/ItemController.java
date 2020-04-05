package slf.xbb.controller;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import slf.xbb.controller.view.ItemVo;
import slf.xbb.error.BussinessException;
import slf.xbb.response.CommonReturnType;
import slf.xbb.service.ItemService;
import slf.xbb.service.impl.ItemServiceImpl;
import slf.xbb.service.model.ItemModel;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/3 8:28 下午
 * @description：Item控制器
 * @modifiedBy：
 * @version:
 */
@Controller("item")
@RequestMapping("/item")
@CrossOrigin(allowedHeaders = "*", allowCredentials = "true")
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;

    /**
     * 创建商品的controller，尽量使controller简单，让service复杂，把服务逻辑聚合在service内部
     *
     * @param title
     * @param desc
     * @param price
     * @param stock
     * @param imgUrl
     * @return
     */
    @RequestMapping(path = "/create", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "desc") String desc,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BussinessException {

        // 封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(desc);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);
        // 调用service业务逻辑
        ItemModel item = itemService.createItem(itemModel);
        //
        ItemVo itemVo = convertFromItemModelToItemVo(itemModel);
        return CommonReturnType.create(itemVo);
    }


    @RequestMapping(path = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) throws BussinessException {

        // 封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel = itemService.getItemById(id);
        //
        ItemVo itemVo = convertFromItemModelToItemVo(itemModel);
        return CommonReturnType.create(itemVo);
    }

    @RequestMapping(path = "/list", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem() throws BussinessException {

        // 封装service请求用来创建商品
        List<ItemModel> itemModelList = itemService.listItem();
        //
        List<ItemVo> itemVoList = itemModelList.stream().map(itemModel -> {
            ItemVo itemVo = convertFromItemModelToItemVo(itemModel);
            return itemVo;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVoList);
    }



    private ItemVo convertFromItemModelToItemVo(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemVo itemVo = new ItemVo();
        BeanUtils.copyProperties(itemModel, itemVo);
        if (itemModel.getPromoModel() != null) {
            // 有正在进行或即将进行的活动
            try {
                // itemVo.getPromoVo().setPromoStatus(itemModel.getPromoModel().getStatus());
                // itemVo.getPromoVo().setPromoId(itemModel.getPromoModel().getId());
                // itemVo.getPromoVo().setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
                // itemVo.getPromoVo().setStartData(itemModel.getPromoModel().getStartDate());
                // itemVo.getPromoVo().setEndData(itemModel.getPromoModel().getEndDate());
                itemVo.setPromoStatus(itemModel.getPromoModel().getStatus());
                itemVo.setPromoId(itemModel.getPromoModel().getId());
                itemVo.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
                itemVo.setPromoStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
                itemVo.setPromoEndDate(itemModel.getPromoModel().getEndDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
                // itemVo.setPromoStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.fullDateTime()));
                // itemVo.setPromoEndDate(itemModel.getPromoModel().getEndDate().toString(DateTimeFormat.fullDateTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            itemVo.setPromoStatus(0);
        }
        return itemVo;
    }
}
