package com.springboot.guo.plugin;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.mysql.jdbc.JDBC42PreparedStatement;
import com.springboot.guo.bean.RouteBean;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author guoqinglin
 * @create 2020-06-03-18:32
 */

@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare",
                args = {Connection.class, Integer.class}),
        @Signature(type = StatementHandler.class, method = "query",
                args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update",
                args = {Statement.class})})
public class RoutePluginImpl implements Interceptor {
    @Autowired
    private DataSource dataSource;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (invocation.getTarget() instanceof StatementHandler) {
            //RoutingStatementHandler
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

            Field delegate = getField(statementHandler, "delegate");
            PreparedStatementHandler prepareStatement = (PreparedStatementHandler) delegate.get(statementHandler);
            Field boundSql = getField(prepareStatement, "boundSql");
            BoundSql bsinstance = (BoundSql) boundSql.get(prepareStatement);

            Field sql = getField(bsinstance, "sql");
            String sqlStr = (String) sql.get(bsinstance);

//            Tb_user user = (Tb_user) bsinstance.getParameterObject();
            RouteBean routeBean = getRouteBean(bsinstance.getParameterObject());
            if (routeBean == null) {
                return invocation.proceed();
            }
            return handler(sqlStr, routeBean, invocation, sql, bsinstance, statementHandler);
        }
        return invocation.proceed();
    }

    private RouteBean getRouteBean(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().isAssignableFrom(RouteBean.class)) {
                try {
                    return (RouteBean) field.get(o);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private Object handler(String sql, RouteBean routeBean, Invocation invocation, Field sqlF, BoundSql bsinstance, StatementHandler statementHandler) {
        if (sql.contains("select") || sql.contains("SELECT")) {
            return handlerSelect(routeBean, sql, invocation, sqlF, bsinstance, statementHandler);
        } else if (sql.contains("insert") || sql.contains("INSERT")) {
            return handlerInsert(routeBean, sql, invocation, sqlF, bsinstance);
        }
        return null;
    }

    private Object handlerInsert(RouteBean routeBean, String sql, Invocation invocation, Field sqlF, BoundSql bsinstance) {
//        PreparedStatement pst = (PreparedStatement)invocation.getArgs()[0];
        try {
            String tableName = getInsertTableName(sql);
            int seq = (int) (routeBean.getPrimaryId() % 3) + 1;
            tableName = tableName + seq;
            sqlF.set(bsinstance, getInsertSql(tableName, sql));
            return invocation.proceed();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getInsertTableName(String sql) {
        String into = sql.substring(sql.indexOf("into") + 4);
        return into.substring(0, into.indexOf("(")).trim();
    }

    private String getInsertSql(String tableName, String sql) {
        String into = sql.substring(0, sql.indexOf("into") + 4);
        String value = sql.substring(sql.indexOf("("));
        return into + " " + tableName + " " + value;
    }

    private String getTableName(String sql) {
        String from = sql.substring(sql.indexOf("from") + 4);
        String tableName = from.substring(0, from.indexOf("where")).trim();
        return tableName;
    }

    private String repalceTableName(String sql, String newTable) {
        String from = sql.substring(0, sql.indexOf("from") + 4);
        String where = sql.substring(sql.indexOf("where"));
        return from + " " + newTable + " " + where;
    }

    private Object handlerSelect(RouteBean routeBean, String sql, Invocation invocation, Field sqlF, BoundSql bsinstance, StatementHandler statementHandler) {
        String tableName = getTableName(sql);
        if (!StringUtils.isEmpty(routeBean.getPrimaryId())) {
            int seq = (int) (routeBean.getPrimaryId() % 3) + 1;
            tableName = tableName + seq;
            try {
                sqlF.set(bsinstance, repalceTableName(sql, tableName));
                return invocation.proceed();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            if ("prepare".equals(invocation.getMethod().getName())) {
                try {
                    return invocation.proceed();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            //id为空，可能是用其它字段查询，这时候就需要每一个表都查询一遍
            try {
                for (int i = 1; i <= routeBean.getTableCount(); i++) {
//                    sqlF.set(bsinstance, repalceTableName(sql, tableName));
//                    modPrepareStatementSql(invocation, i);
                    //这里在调用被代理类中的query方法之前必须重新创建Preparestatement对象
                    Statement statement = getStatement(statementHandler, sqlF, tableName + i, bsinstance, sql);
                    //重新生成新sql的statement对象并替换
                    invocation.getArgs()[0] = statement;
                    List<Object> list = (List<Object>) invocation.proceed();
                    if (list != null && list.size() > 0) {
                        return list;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Log getStatementLog(StatementHandler statementHandler) {
        try {
            Field delegate = getField(statementHandler, "delegate");
            PreparedStatementHandler prepareStatement = (PreparedStatementHandler) delegate.get(statementHandler);
            Field mappedStatement = getField(prepareStatement, "mappedStatement");
            MappedStatement mappedStatementO = (MappedStatement) mappedStatement.get(prepareStatement);
            return mappedStatementO.getStatementLog();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Statement getStatement(StatementHandler statementHandler, Field sqlF, String tableName, BoundSql bsinstance, String sql) {
        try {
            sqlF.set(bsinstance, repalceTableName(sql, tableName));
            Connection connection = DataSourceUtils.getConnection(dataSource);
            Connection connectionProxy = ConnectionLogger.newInstance(connection, getStatementLog(statementHandler), 1);
            Statement statement = statementHandler.prepare(connectionProxy, null);
            statementHandler.parameterize(statement);
            return statement;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void modPrepareStatementSql(Invocation invocation, int i) {
        Statement statement = (Statement) invocation.getArgs()[0];
        Field h = getField(statement, "h");
        try {
            InvocationHandler o = (InvocationHandler) h.get(statement);
            Field statement1 = getField(o, "statement");
            DruidPooledPreparedStatement dp = (DruidPooledPreparedStatement) statement1.get(o);
            Field sql1 = getField(dp, "sql");
            String sql1Str = (String) sql1.get(dp);
            String tableName1 = getTableName(sql1Str);
            String newSql1 = repalceTableName(sql1Str, tableName1 + i);
            sql1.set(dp, newSql1);

            Field stmt = getField(dp, "stmt");
            PreparedStatement ps = (PreparedStatement) stmt.get(dp);
            Field sql = getField(ps, "sql");
            String sqlStr = (String) sql.get(ps);
            String tableName = getTableName(sqlStr);
            String newSql = repalceTableName(sqlStr, tableName + i);
            sql.set(ps, newSql);

            Field statement2 = getField(ps, "statement");
            JDBC42PreparedStatement o1 = (JDBC42PreparedStatement) statement2.get(ps);
            Field originalSql = getField(o1, "originalSql");
            String sql2Str = (String) originalSql.get(o1);
            String tableName2 = getTableName(sql2Str);
            String newSql2 = repalceTableName(sql2Str, tableName2 + i);
            originalSql.set(o1, newSql2);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Field getField(Object o, String name) {
        Field field = ReflectionUtils.findField(o.getClass(), name);
        ReflectionUtils.makeAccessible(field);
        return field;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof RoutingStatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }
}
