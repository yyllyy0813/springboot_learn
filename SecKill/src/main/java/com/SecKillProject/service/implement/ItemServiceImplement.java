package com.SecKillProject.service.implement;

import com.SecKillProject.dao.ItemDOMapper;
import com.SecKillProject.dao.ItemStockDOMapper;
import com.SecKillProject.dataobject.ItemDO;
import com.SecKillProject.dataobject.ItemStockDO;
import com.SecKillProject.error.BussinessException;
import com.SecKillProject.error.EmBussinessError;
import com.SecKillProject.service.ItemService;
import com.SecKillProject.service.PromoService;
import com.SecKillProject.service.model.ItemModel;
import com.SecKillProject.service.model.PromoModel;
import com.SecKillProject.validator.ValidationResult;
import com.SecKillProject.validator.ValidatorImplement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImplement implements ItemService {

    private static final Integer IS_END = 3;
    @Autowired
    private ValidatorImplement validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BussinessException {

        //校验入参
        ValidationResult validationResult = validator.validate(itemModel);
        if(validationResult.isHasErrors()){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,validationResult.getErrMsg());
        }

        //转化itemmodel为dataobject
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);

        //写入数据库
        itemDOMapper.insertSelective(itemDO);
        //System.out.println(itemDO.getId());
        itemModel.setId(itemDO.getId());
        //System.out.println(itemModel.getId());

        ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(itemModel);

        itemStockDOMapper.insertSelective(itemStockDO);

        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listitem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());

        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {

        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO == null){
            return null;
        }

        //操作获得库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        //将dataobject转化为model
        ItemModel itemModel = this.convertModelFromDataObject(itemDO,itemStockDO);

        //获得活动商品信息
        PromoModel promoModel = promoService.getPromoModelByItemId(itemModel.getId());
        if(promoModel != null && promoModel.getStatus()!=IS_END){
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BussinessException {

        int affectedRow = itemStockDOMapper.decreaseStock(itemId,amount);
        if (affectedRow > 0){
            //更新库存成功
            return true;
        }else{
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BussinessException {
        itemDOMapper.increaseSales(itemId,amount);
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());

        return itemModel;
    }

}
