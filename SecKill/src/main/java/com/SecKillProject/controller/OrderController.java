package com.SecKillProject.controller;

import com.SecKillProject.controller.BaseController;
import com.SecKillProject.error.BussinessException;
import com.SecKillProject.error.EmBussinessError;
import com.SecKillProject.response.CommonReturnType;
import com.SecKillProject.service.OrderService;
import com.SecKillProject.service.model.OrderModel;
import com.SecKillProject.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller("order")
@RequestMapping(value = "/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
                                        @RequestParam(name = "amount")Integer amount,
                                        @RequestParam(name = "promoId",required = false)Integer promoId) throws BussinessException {

        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(isLogin == null || !isLogin.booleanValue()){
            throw new BussinessException(EmBussinessError.USER_NOT_LOGIN,"用户未登录");
        }
        //获取用户登录信息
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");


        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,promoId,amount);

        return CommonReturnType.create(null);
    }
}
