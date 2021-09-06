package com.nianhen.example.service;

import com.nianhen.springlite.annotation.Autowired;
import com.nianhen.springlite.annotation.Component;

/**
 * @description: 用户服务
 * @author: nianhen
 * @since: 2021-09-04 09:43
 **/
@Component
public class UserService implements IUserService{

    @Autowired
    private OrderService orderServiceImpl;

    @Autowired
    private BeanNameAwareService beanNameAwareService;

    @Override
    public void test() {
        System.out.println("test in userService");
    }

    @Override
    public void testOrder() {
        String order = orderServiceImpl.getOrder();
        System.out.println("getOrder in userService: " + order);
    }

    @Override
    public void testAware() {
        String name = beanNameAwareService.getName();
        System.out.println("testAware in userService: " + name);
    }

    @Override
    public void testProxy() {
        System.out.println("testProxy in userService");
    }
}
