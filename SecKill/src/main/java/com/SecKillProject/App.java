package com.SecKillProject;

import com.SecKillProject.dao.UserDataObjectMapper;
import com.SecKillProject.dataobject.UserDataObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = {"com.SecKillProject"})
@RestController
@MapperScan("com.SecKillProject.dao")
public class App
{
    @Autowired
    private UserDataObjectMapper userDataObjectMapper;

    @RequestMapping("/")
    public String home(){
        UserDataObject UserDataObject = userDataObjectMapper.selectByPrimaryKey(1);
        if(UserDataObject == null){
            return "用户对象不存在";
        }else{
            return UserDataObject.getName();
        }
    }
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class,args);
    }
}
