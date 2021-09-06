package com.nianhen.springlite.inter;

/**
 * @description: Bean后置处理器接口
 * @author: nianhen
 * @since: 2021-09-04 16:18
 **/
public interface BeanPostProcessor {

    /**
     * Bean初始化前  Spring后置初期器
     *
     * @param beanName Bean名称
     * @param bean     Bean对象
     * @return 新的Bean对象
     */
    Object postProcessBeforeInitialization(String beanName, Object bean);

    /**
     * Bean初始化后  Spring后置初期器
     *
     * @param beanName Bean名称
     * @param bean     Bean对象
     * @return 新的Bean对象
     */
    Object postProcessAfterInitialization(String beanName, Object bean);
}
