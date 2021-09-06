package com.nianhen.example.service;

import com.nianhen.springlite.annotation.Component;

/**
 * @description: 订单服务
 * @author: nianhen
 * @since: 2021-09-04 09:44
 **/
@Component("orderServiceImpl")
public class OrderService {

    public String getOrder() {
        return "getOrder";
    }
}
