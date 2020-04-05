package slf.xbb.service.impl;

import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import slf.xbb.dao.PromoDoMapper;
import slf.xbb.domain.PromoDo;
import slf.xbb.service.PromoService;
import slf.xbb.service.model.PromoModel;

import javax.xml.crypto.Data;
import java.sql.DataTruncation;

/**
 * @author ：xbb
 * @date ：Created in 2020/4/5 9:33 上午
 * @description：促销秒杀服务实现
 * @modifiedBy：
 * @version:
 */
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    PromoDoMapper promoDoMapper;

    /**
     * 通过itemId获取促销秒杀信息，才能对前端显示做修改
     *
     * @param itemId
     * @return
     */
    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDo promoDo = promoDoMapper.selectByItemId(itemId);
        PromoModel promoModel = covertFromPromoDoToPromoModel(promoDo);
        if (promoModel == null) {
            return null;
        }

        // 判断当前时间，秒杀活动是否即将开始或开始进行
        int status = NowDateCompareToPromoDate(promoModel.getStartDate(), promoModel.getEndDate());
        promoModel.setStatus(status);
        return promoModel;
    }


    /**
     * 返回秒杀活动状态
     * 1、未开始
     * 2、进行
     * 3、结束
     *
     * @param start
     * @param end
     * @return
     */
    private int NowDateCompareToPromoDate(DateTime start, DateTime end) {
        if (start.isAfterNow()) {
            return 1;
        } else if (end.isBeforeNow()) {
            return 3;
        } else {
            return 2;
        }
    }

    private PromoModel covertFromPromoDoToPromoModel(PromoDo promoDo) {
        if (promoDo == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDo, promoModel);
        promoModel.setStartDate(new DateTime(promoDo.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDo.getEndDate()));
        return promoModel;
    }
}
