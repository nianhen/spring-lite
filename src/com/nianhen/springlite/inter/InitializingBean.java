package com.nianhen.springlite.inter;

/**
 * @description: Bean初始化接口
 * @author: nianhen
 * @since: 2021-09-04 16:17
 **/
public interface InitializingBean {

    /**
     * Bean属性填充后执行，可通过该方法初始化Bean中属性的值
     */
    void afterPropertiesSet();
}
