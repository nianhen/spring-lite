package com.nianhen.example;

import com.nianhen.example.config.AppConfig;
import com.nianhen.example.service.BeanNameAwareService;
import com.nianhen.example.service.IUserService;
import com.nianhen.example.service.UserService;
import com.nianhen.springlite.SpringLiteApplicationContext;
import com.nianhen.springlite.annotation.Autowired;

/**
 * @description: SpringLite项目启动类
 * @author: nianhen
 * @since: 2021-09-03 22:36
 **/
public class MyApplication {

    public static void main(String[] args) {
        SpringLiteApplicationContext myApp = new SpringLiteApplicationContext(AppConfig.class);

        IUserService userService = (IUserService) myApp.getBean("userService");
        userService.test();
        userService.testOrder();
        userService.testAware();
        userService.testProxy();
    }

}
