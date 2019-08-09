package com.SecKillProject.controller;

import com.SecKillProject.controller.viewobject.UserVO;
import com.SecKillProject.error.BussinessException;
import com.SecKillProject.error.EmBussinessError;
import com.SecKillProject.response.CommonReturnType;
import com.SecKillProject.service.UserService;
import com.SecKillProject.service.model.UserModel;
import com.alibaba.druid.util.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telephone")String telephone,
                                  @RequestParam(name = "password")String password) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if(org.apache.commons.lang3.StringUtils.isEmpty(telephone)||
                org.apache.commons.lang3.StringUtils.isEmpty(password)){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
        //校验用户登录是否合法
        UserModel userModel = userService.validateLogin(telephone,this.EncodeByMd5(password));

        //将登录凭证加入到用户登陆成功的session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        return CommonReturnType.create(null);
    }

    //用户注册接口
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telephone")String telephone,
                                     @RequestParam(name = "otpCode")String otpCode,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Integer gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "password")String password) throws BussinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和Otp是否相符
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telephone);
        if(!com.alibaba.druid.util.StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符");
        }
        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(Byte.valueOf(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelephone(telephone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    //用户获取otp短信接口
    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telephone")String telephone){
        //生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(89999);
        randomInt += 10000;  //此时随机数取[10000,99999]
        String otpCode = String.valueOf(randomInt);

        //将OTP验证码同对应用户的手机号关联
        httpServletRequest.getSession().setAttribute(telephone,otpCode);

        //将OTP验证码通过短信通道发送给用户
        System.out.println("telephone = " + telephone + "&otpCode = " + otpCode);

        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        Base64 base64en = new Base64();
        String newstr = base64en.encodeAsString(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id")Integer id) throws BussinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserByid(id);

        if(userModel == null){
//            userModel.setEncrptPassword("123");
            throw new BussinessException(EmBussinessError.USER_NOT_EXIST);
        }

        UserVO userVO =  convertFromModel(userModel);

        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }
}




