package com.SecKillProject.service;

import com.SecKillProject.error.BussinessException;
import com.SecKillProject.service.model.UserModel;

public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserByid(Integer id);
    void register(UserModel userModel) throws BussinessException;
    UserModel validateLogin(String telephone,String encreptPassword) throws BussinessException;
}
