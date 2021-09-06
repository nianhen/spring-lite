package com.nianhen.example.service;

import com.nianhen.springlite.annotation.Component;
import com.nianhen.springlite.inter.InitializingBean;

/**
 * @description: InitializingBean接口测试服务
 * @author: nianhen
 * @since: 2021-09-04 16:30
 **/
@Component
public class InitializingBeanService implements InitializingBean {

    private String testField;

    @Override
    public void afterPropertiesSet() {
        this.testField = "Talk is cheap, show me the code!";
    }

    public String getTestField() {
        return testField;
    }
}
