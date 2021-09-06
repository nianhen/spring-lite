package com.nianhen.example.service;

import com.nianhen.springlite.annotation.Component;
import com.nianhen.springlite.inter.BeanPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @description: 后置处理器测试服务
 * @author: nianhen
 * @since: 2021-09-06 21:19
 **/
@Component
public class BeanPostProcessorService implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        System.out.println("-------------invoke postProcessBeforeInitialization for " + beanName + "-------------");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(String beanName, Object bean) {
        System.out.println("-------------invoke postProcessAfterInitialization for " + beanName + "-------------");
        if ("userService".equals(beanName)) {
            System.out.println("----------userService替换为代理对象----------");
            return Proxy.newProxyInstance(BeanPostProcessorService.class.getClassLoader(), bean.getClass().getInterfaces(), (proxy, method, args) -> {
                System.out.println("----------切面Before逻辑----------");
                Object result = method.invoke(bean, args);
                System.out.println("----------切面After逻辑----------");
                return result;
            });

        }
        return bean;
    }
}
