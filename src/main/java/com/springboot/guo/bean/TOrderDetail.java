package com.springboot.guo.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author guoqinglin
 * @create 2020-06-08-10:29
 */
@Getter
@Setter
public class TOrderDetail {
    public Long orderDetailId;

    public Long orderId;

    public String orderDetailName;

    public Date createTime;
}
