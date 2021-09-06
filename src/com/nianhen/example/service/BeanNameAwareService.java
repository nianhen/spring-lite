package com.nianhen.example.service;

import com.nianhen.springlite.annotation.Component;
import com.nianhen.springlite.inter.BeanNameAware;

/**
 * @description: BeanNameAware测试服务
 * @author: nianhen
 * @since: 2021-09-06 21:21
 **/
@Component
public class BeanNameAwareService implements BeanNameAware {

    private String name = "";

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
