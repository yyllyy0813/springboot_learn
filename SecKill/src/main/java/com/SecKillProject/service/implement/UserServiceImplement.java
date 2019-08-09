package com.SecKillProject.service.implement;

import com.SecKillProject.dao.UserDataObjectMapper;
import com.SecKillProject.dao.UserPwdDataObjectMapper;
import com.SecKillProject.dataobject.UserDataObject;
import com.SecKillProject.dataobject.UserPwdDataObject;
import com.SecKillProject.error.BussinessException;
import com.SecKillProject.error.EmBussinessError;
import com.SecKillProject.service.UserService;
import com.SecKillProject.service.model.UserModel;
import com.SecKillProject.validator.ValidationResult;
import com.SecKillProject.validator.ValidatorImplement;
import com.alibaba.druid.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImplement implements UserService {

    @Autowired
    private UserDataObjectMapper userDataObjectMapper;

    @Autowired
    private UserPwdDataObjectMapper userPwdDataObjectMapper;

    @Autowired
    private ValidatorImplement validator;

    @Override
    public UserModel getUserByid(Integer id) {
        //调用userdomapper获取到对应的用户dataobject
        UserDataObject userDataObject = userDataObjectMapper.selectByPrimaryKey(id);
        if(userDataObject == null){
            return null;
        }
        UserPwdDataObject userPwdDataObject = userPwdDataObjectMapper.selectByUserId(userDataObject.getId());
        return convertFromDataObject(userDataObject,userPwdDataObject);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BussinessException {
        if (userModel == null){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
        }
//        if(StringUtils.isEmpty(userModel.getName())
//            || userModel.getGender() == null
//            || userModel.getAge() ==null
//            || StringUtils.isEmpty(userModel.getTelephone())){
//            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR);
//        }
        ValidationResult validationResult = validator.validate(userModel);
        if(validationResult.isHasErrors()){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,validationResult.getErrMsg());
        }

        UserDataObject userDataObject = convertFromModel(userModel);
        try{
            userDataObjectMapper.insertSelective(userDataObject);
        }catch(DuplicateKeyException ex){
            throw new BussinessException(EmBussinessError.PARAMETER_VALIDATION_ERROR,"手机号已被注册");
        }

        userModel.setId(userDataObject.getId());

        UserPwdDataObject userPwdDataObject = convertPwdFromModel(userModel);
        userPwdDataObjectMapper.insertSelective(userPwdDataObject);

        return;
    }

    @Override
    public UserModel validateLogin(String telephone, String encreptPassword) throws BussinessException {
        //通过手机获取用户信息
        UserDataObject userDataObject = userDataObjectMapper.selectByTelephone(telephone);
        if(userDataObject == null){
            throw new BussinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        UserPwdDataObject userPwdDataObject = userPwdDataObjectMapper.selectByUserId(userDataObject.getId());
        UserModel userModel = convertFromDataObject(userDataObject,userPwdDataObject);

        //对比密码
        if(!StringUtils.equals(encreptPassword,userModel.getEncrptPassword())){
            throw new BussinessException(EmBussinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    private UserDataObject convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserDataObject userDataObject = new UserDataObject();
        BeanUtils.copyProperties(userModel,userDataObject);
        return userDataObject;
    }

    private UserPwdDataObject convertPwdFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserPwdDataObject userPwdDataObject = new UserPwdDataObject();
        userPwdDataObject.setEncrptPassword(userModel.getEncrptPassword());
        userPwdDataObject.setUserId(userModel.getId());
        return userPwdDataObject;
    }

    private UserModel convertFromDataObject(UserDataObject userDataObject, UserPwdDataObject userPwdDataObject){

        if(userDataObject == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDataObject,userModel);
        if(userPwdDataObject != null){
            userModel.setEncrptPassword(userPwdDataObject.getEncrptPassword());
        }
        return userModel;
    }
}
