package com.springboot.guo.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * @author guoqinglin
 * @create 2020-06-03-16:01
 */

@Getter
@Setter
public class Tb_user {
    public Long user_id;

    public String user_name;

    public Integer seq;

    //properties配置文件
    public RouteBean routeBean;
}
