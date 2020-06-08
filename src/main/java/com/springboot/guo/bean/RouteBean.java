package com.springboot.guo.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * @author guoqinglin
 * @create 2020-06-03-16:02
 */

@Getter
@Setter
public class RouteBean {
    private int tableCount;

    //如果要用id查询的话，需要设置该值
    private Long primaryId;
}
