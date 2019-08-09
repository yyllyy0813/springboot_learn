package com.SecKillProject.service.implement;

import com.SecKillProject.dao.PromoDOMapper;
import com.SecKillProject.dataobject.PromoDO;
import com.SecKillProject.service.PromoService;
import com.SecKillProject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PromoServiceImplement implements PromoService {

    private static final Integer NOT_START = 1;
    private static final Integer ON_GOING = 2;
    private static final Integer IS_END =3;

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Override
    public PromoModel getPromoModelByItemId(Integer itemId) {

        //获取秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        PromoModel promoModel = this.convertModelFromDataObject(promoDO);
        if(promoModel == null){
            return null;
        }

        //判断当前时间秒杀活动即将开始或正在进行
        DateTime dateTime = new DateTime();
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(NOT_START);
        }else if (promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(IS_END);
        }else{
            promoModel.setStatus(ON_GOING);
        }

        return promoModel;
    }

    private PromoModel convertModelFromDataObject(PromoDO promoDO){
        if(promoDO == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();

        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
}
