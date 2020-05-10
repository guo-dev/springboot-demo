package com.springboot.guo.dynamicDataSource;

import com.springboot.guo.dynamicDataSource.DynamicDataSourceContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(-1)
@Component
public class DynamicDataSourceAspect {
    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSourceAspect.class);
    
    @Before("@annotation(ds)")
    public void changeDataSource(JoinPoint point, TargetDataSource ds) {
        //这个就是数据源标识
        String dsId = ds.name();
        
        if (!DynamicDataSourceContextHolder.containsDataSource(dsId)) {
            logger.error("数据源【{}】不存在，使用默认数据源 > {}",
                    ds.name(),
                    point.getSignature());
        }
        else {
            logger.debug("Use dataSource : {} > {}",
                    ds.name(),
                    point.getSignature());
            //如果容器中有数据源，那么就把数据源标识设置到ThreadLocal中
            DynamicDataSourceContextHolder.setDataSourceType(dsId);
        }
    }

    @After("@annotation(ds)")
    public void releaseLocal(JoinPoint point, TargetDataSource ds) {
        logger.info("==释放ds：" + ds.name() + "的ThreadLocal绑定==");
        if(DynamicDataSourceContextHolder.getDataSourceType() != null) {
            DynamicDataSourceContextHolder.getContextHolder().remove();
        }
    }
}
