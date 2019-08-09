package com.SecKillProject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;


//注解表示它是一个spring的beaan
@Component
public class ValidatorImplement implements InitializingBean {

    private Validator validator;

    //实现校验方法并返回校验结果
    public ValidationResult validate(Object bean){
        ValidationResult validationResult = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if(constraintViolationSet.size() > 0){
            validationResult.setHasErrors(true);
            constraintViolationSet.forEach(constraintViolation->{
               String errMsg = constraintViolation.getMessage();
               String propertyName = constraintViolation.getPropertyPath().toString();
               validationResult.getErrorMsgMap().put(propertyName,errMsg);
            });
        }
        return validationResult;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
