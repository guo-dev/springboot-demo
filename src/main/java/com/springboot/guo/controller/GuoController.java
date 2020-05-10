package com.springboot.guo.controller;

import com.springboot.guo.bean.ConsultConfigArea;
import com.springboot.guo.dynamicDataSource.DynamicDataSource;
import com.springboot.guo.service.AreaService;
import com.springboot.guo.service.AreaServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.*;

@Controller
@Api(tags = "springboot学习工程相关接口")
public class GuoController {
    
    private static final Logger logger = LoggerFactory.getLogger(GuoController.class);
    
    @Autowired
    AreaService areaService;

//    @Resource
//    private DynamicDataSource dataSource;
//
//    @Bean
//    public SqlSessionFactory sqlSessionFactory() throws Exception{
//        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//        sqlSessionFactoryBean.setDataSource(dataSource);
//        return sqlSessionFactoryBean.getObject();
//    }



    @Value("${application.field:default value guo}")
    private String springbootField = "";

    @ApiOperation("jsp测试接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", defaultValue = "jack"),
            @ApiImplicitParam(name = "address", value = "用户地址", defaultValue = "长沙")
    })
    @RequestMapping("/index")
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("index");
        mv.addObject("time", new Date());
        mv.addObject("message", springbootField);
        return mv;
    }

    @ApiOperation("freemarker测试接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "map", value = "返回值")
    })
    @RequestMapping("/freemarker")
    public String freemarker(Map<String, Object> map) {
        
        map.put("name", "springboot guo");
        map.put("gender", 0);
        
        List<Map<String, Object>> friends = new ArrayList<Map<String, Object>>();
        Map<String, Object> friend = new HashMap<String, Object>();
        friend.put("name", "roy");
        friend.put("age", 32);
        friends.add(friend);
        friend = new HashMap<String, Object>();
        friend.put("name", "walker");
        friend.put("age", 34);
        friends.add(friend);
        map.put("friends", friends);
        return "freemarker";
    }

    @ApiOperation("查询地区接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "param", value = "地区编码")
    })
    @RequestMapping("/queryArea")
    public @ResponseBody
    String queryArea(String param) {
        List<ConsultConfigArea> areas = areaService.qryArea(new HashMap());
        for (ConsultConfigArea area : areas) {
            logger.info(area.getAreaCode() + "   " + area.getAreaName() + "   "
                    + area.getState());
        }
        return "OK";
    }

    @RequestMapping("/testDevTool")
    public @ResponseBody
    String testDevTool() {
        return "OK";
    }

    @RequestMapping("/testDevTool1")
    public @ResponseBody
    String testDevTool1() {
        return "OK";
    }
}
