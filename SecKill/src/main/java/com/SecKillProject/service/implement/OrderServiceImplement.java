package com.SecKillProject.service.implement;

import com.SecKillProject.dao.OrderDOMapper;
import com.SecKillProject.dao.SequenceDOMapper;
import com.SecKillProject.dataobject.OrderDO;
import com.SecKillProject.dataobject.SequenceDO;
import com.SecKillProject.error.BussinessException;
import com.SecKillProject.error.EmBussinessError;
import com.SecKillProject.service.ItemService;
import com.SecKillProject.service.OrderService;
import com.SecKillProject.service.UserService;
import com.SecKillProject.service.model.ItemModel;
import com.SecKillProject.service.model.OrderModel;
import com.SecKillProject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImplement implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BussinessException {

        //1.校验下单状态，商品、用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }

        UserModel userModel = userService.getUserByid(userId);
        if (userModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"用户信息不存在");
        }

        if (amount <= 0 || amount >= 99){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"数量信息不合法");
        }

        //校验活动信息
        if(promoId.intValue()!=itemModel.getPromoModel().getItemId()){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
        }else if(itemModel.getPromoModel().getStatus().intValue() != 2){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"活动还未开始");
        }


        //2.下单成功减库存（落单减库存，支付减库存）
        boolean result = itemService.decreaseStock(itemId,amount);
        if(!result){
            throw new BussinessException(EmBussinessError.STOCK_NOT_ENOUGH);
        }

        //3.订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if (promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }
//        orderModel.setItemPrice(itemModel.getPrice());
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号
        orderModel.setId(generateOrderNumber());

        OrderDO orderDO = this.convertOrderDOFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        itemService.increaseSales(itemId,amount);
        
        //4.返回前端
        return orderModel;
    }

//    public static void main(String[] args){
//        LocalDateTime currentTime = LocalDateTime.now();
//        System.out.println(currentTime.format(DateTimeFormatter.ISO_DATE).replace("-",""));
//    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected String generateOrderNumber(){

        //订单号有16位
        StringBuilder orderStringBuilder = new StringBuilder();

        //前8位为年月日
        LocalDateTime currentTime = LocalDateTime.now();
        String orderTime = currentTime.format(DateTimeFormatter.ISO_DATE).replace("-","");
        orderStringBuilder.append(orderTime);

        //中间6位为自增序列
        //获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequence+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for(int i=1;i<=6-sequenceStr.length();i++){
            orderStringBuilder.append(0);
        }
        orderStringBuilder.append(sequenceStr);

        //最后两位分库分表位
        orderStringBuilder.append("00");

        return orderStringBuilder.toString();
    }

    private OrderDO convertOrderDOFromOrderModel(OrderModel orderModel){
        if(orderModel == null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
