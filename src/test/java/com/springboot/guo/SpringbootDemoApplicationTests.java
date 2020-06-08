package com.springboot.guo;

import com.springboot.guo.bean.*;
import com.springboot.guo.dao.CommonMapper;
import com.springboot.guo.util.SnowflakeUtil;
import com.alibaba.fastjson.JSONObject;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringbootDemoApplication.class)
class SpringbootDemoApplicationTests {

    @Autowired
    private CommonMapper commonMapper;

    @Test
    public void test1() {
        ConsultConfigArea area = new ConsultConfigArea();
        area.setAreaCode("peter");
        area.setAreaName("peter");
        area.setState(2);
        commonMapper.addArea(area);

    }

    @Test//Mycat mod-long分片测试，操作t_user表，会路由到t_user$1-3
    public void test2() {
//        for (int i = 0; i < 100; i++) {//插入
//            Tb_user user = new Tb_user();
//            long id = SnowflakeUtil.nextId();
//            user.setUser_id(id);
//            user.setUser_name("Guo"+i);
//            commonMapper.addT_user(user);
//        }
        Tb_user user = new Tb_user();
        user.setUser_id(718435237271441408L);
        List<Tb_user> t_users = commonMapper.queryUser(user);
        System.out.println(JSONObject.toJSONString(t_users));


    }



    @Test
    public void test3() {
        ZgGoods zgGoods = new ZgGoods();
        zgGoods.setGoodCode("Jack");
        zgGoods.setGoodName("Jack");
        zgGoods.setCount(100);
        commonMapper.addGood(zgGoods);
    }

    @Test
    public void test6(){
        for (int i = 0; i < 100; i++) {
            long id = SnowflakeUtil.nextId();
            long l = id %3;
            Tb_user user = new Tb_user();
            user.setUser_id(id);
            user.setUser_name("James"+i);
            user.setSeq((int)l+1);
            commonMapper.addT_user(user);
        }
    }

    @Test
    public void test7() {
        for (int i = 0; i < 100; i++) {
            Long id = SnowflakeUtil.nextId();
            Tb_user user = new Tb_user();
            user.setUser_id(id);
            user.setUser_name("Peter" + i);
            //有侵入性。。进行路由数据的配置
            RouteBean routeBean = new RouteBean();
            routeBean.setPrimaryId(id);
            routeBean.setTableCount(3);
            user.setRouteBean(routeBean);
//            user.setRoute(true);
            commonMapper.addT_user(user);
        }
    }

    @Test
    public void test8() {

        RouteBean routeBean = new RouteBean();
        routeBean.setTableCount(3);
    //    routeBean.setPrimaryId(717823970809745408L);
        Tb_user user = new Tb_user();
   //     user.setUser_id(717823970809745408L);
        user.setUser_name("Peter62");
        user.setRouteBean(routeBean);

        List<Tb_user> user1 = commonMapper.queryUser(user);
        System.out.println(JSONObject.toJSONString(user1));
    }

    /*
     *一致性hash
     * */
    @Test
    public void test9() {
        for (int i = 0; i <= 1000; i++) {
            Long id = SnowflakeUtil.nextId();
            T_order order = new T_order();
            order.setOrderId(id);
            order.setOrderName("Jack" + i);
            commonMapper.addTt_order_murmur_hash(order);
        }

        for (int i = 1001; i <= 2000; i++) {
            Long id = SnowflakeUtil.nextId();
            T_order order = new T_order();
            order.setOrderId(id);
            order.setOrderName("Deer" + i);
            commonMapper.addTt_order_murmur_hash(order);
        }
    }

    /**
     * 分库测试用例
     */
    @Test//分库测试用例
    public void addTOrder() {
        for (int i = 0; i <= 1000; i++) {
            long id = SnowflakeUtil.nextId();
            T_order order = new T_order();
            order.setOrderId(id);
            order.setOrderName("Jack" + i);
            order.setOrderType("NZ");
            commonMapper.addTOrder(order);
        }
    }

    /**
     * 分片枚举
     */
    @Test
    public void addHashInt(){
        T_order_sharing_by_intfile order = new T_order_sharing_by_intfile();
        order.setOrderId(SnowflakeUtil.nextId());
        order.setOrderName("Peter");
        order.setProvince("xizang");
        commonMapper.addT_order_sharing_by_intfile(order);
    }

    /**
     * 固定hash分片
     */
    @Test
    public void addt_order_gd_hash(){
        for (int i = 0; i < 100; i++) {
            T_order order = new T_order();
            order.setOrderId(SnowflakeUtil.nextId());
            order.setOrderName("Jack"+i);

            commonMapper.addTt_order_gd_hash(order);
        }
    }

    @Test
    public void test11(){
        T_order order = new T_order();
        Long id = SnowflakeUtil.nextId();
        order.setOrderId(id);
        order.setOrderName("deer1012");
        order.setCreateTime(new Timestamp(new Date("2020/07/29").getTime()));
        commonMapper.addTt_order_time_day(order);
    }

    @Test//全局表插入数据
    public void globalTableSave(){
        TOrderType tOrderType =new TOrderType();
        tOrderType.setOrderType("BJm");
        tOrderType.setOrderTypeName("保健m");
        commonMapper.addTOrderType(tOrderType);
    }

    @Test
    public void globalTableQuery(){
        TOrderType tOrderType = new TOrderType();
        tOrderType.setOrderType("BJ");
        System.out.println(JSONObject.toJSONString(commonMapper.queryTOrderType(tOrderType)));
    }

    @Test//ER分片测试用例
    public void ERTable() {
        for (int i = 0; i < 100; i++) {
            Long id = SnowflakeUtil.nextId();
            T_order order = new T_order();
            order.setOrderId(Long.valueOf(i));
            order.setOrderName("Jack" + i);
            order.setCreateTime(new Timestamp(new Date("2020/05/25").getTime()));
            order.setOrderType("NVZ");
            commonMapper.addTOrder(order);

            TOrderDetail tOrderDetail = new TOrderDetail();
            tOrderDetail.setOrderDetailId(SnowflakeUtil.nextId());
            tOrderDetail.setOrderDetailName("Jack" + i);
            tOrderDetail.setOrderId(Long.valueOf(i));
            commonMapper.addTOrderDetail(tOrderDetail);
        }
    }

    @Test
    void contextLoads() {
    }

}
