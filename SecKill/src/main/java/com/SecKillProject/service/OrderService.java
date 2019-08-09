package com.SecKillProject.service;

import com.SecKillProject.error.BussinessException;
import com.SecKillProject.service.model.OrderModel;

public interface OrderService {
    OrderModel createOrder(Integer userId,Integer itemId,Integer promoId,Integer amount) throws BussinessException;

}
