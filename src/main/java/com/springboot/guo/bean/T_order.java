package com.springboot.guo.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author guoqinglin
 * @create 2020-06-06-12:54
 */

@Setter
@Getter
public class T_order {
    public Long orderId;

    public String orderName;

    public String orderType;

    public Date createTime;
}
