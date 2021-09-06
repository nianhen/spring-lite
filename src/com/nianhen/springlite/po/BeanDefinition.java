package com.nianhen.springlite.po;

/**
 * @description: Bean定义
 * @author: nianhen
 * @since: 2021-09-03 23:16
 **/
public class BeanDefinition {
    private Class<?> beanClass;
    private ScopeEnum scope;
    private String beanName;

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public ScopeEnum getScope() {
        return scope;
    }

    public void setScope(ScopeEnum scope) {
        this.scope = scope;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

}
