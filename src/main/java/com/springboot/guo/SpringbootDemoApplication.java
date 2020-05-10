package com.springboot.guo;

import com.springboot.guo.druidConfig.DruidConfig;
import com.springboot.guo.dynamicDataSource.DataSourceConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.springboot.guo")
@MapperScan(value = "com.springboot.guo.dao")
//@ComponentScan(basePackages = {"com.springboot.guo.dao"})
@ServletComponentScan(basePackages = {"com.springboot.guo"})
@EnableConfigurationProperties(DruidConfig.class)
public class SpringbootDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootDemoApplication.class, args);
    }

}
