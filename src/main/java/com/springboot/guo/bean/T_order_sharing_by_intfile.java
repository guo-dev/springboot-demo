package com.springboot.guo.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author guoqinglin
 * @create 2020-06-06-13:31
 */
@Getter
@Setter
public class T_order_sharing_by_intfile {
    public Long orderId;
    public String orderName;
    public Date createTime;
    public String province;

}
