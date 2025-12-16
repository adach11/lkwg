package com.seele.game.config;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * MyBatis配置类
 */
@Configuration
public class MyBatisConfig {

    /**
     * 注册时间戳拦截器
     */
    @Bean
    public TimestampInterceptor timestampInterceptor() {
        return new TimestampInterceptor();
    }

    /**
     * 时间戳自动填充拦截器
     * 拦截 INSERT 和 UPDATE 操作，自动填充时间戳字段
     */
    @Intercepts({
        @Signature(type = Executor.class, method = "update",
                   args = {MappedStatement.class, Object.class})
    })
    public static class TimestampInterceptor implements Interceptor {

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
            Object parameter = invocation.getArgs()[1];

            if (parameter != null) {
                if (SqlCommandType.INSERT.equals(sqlCommandType)) {
                    // 插入时自动填充创建时间和更新时间
                    setFieldValue(parameter, "createdAt", LocalDateTime.now());
                    setFieldValue(parameter, "updatedAt", LocalDateTime.now());
                    setFieldValue(parameter, "learnedAt", LocalDateTime.now());
                } else if (SqlCommandType.UPDATE.equals(sqlCommandType)) {
                    // 更新时自动填充更新时间
                    setFieldValue(parameter, "updatedAt", LocalDateTime.now());
                }
            }

            return invocation.proceed();
        }

        /**
         * 设置字段值（只在字段为 null 时设置）
         */
        private void setFieldValue(Object obj, String fieldName, Object value) {
            try {
                Field field = findField(obj.getClass(), fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    if (field.get(obj) == null) {  // 只在字段为 null 时设置
                        field.set(obj, value);
                    }
                }
            } catch (Exception e) {
                // 静默忽略字段不存在的情况
            }
        }

        /**
         * 递归查找字段（包括父类）
         */
        private Field findField(Class<?> clazz, String fieldName) {
            while (clazz != null) {
                try {
                    return clazz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            return null;
        }

        @Override
        public Object plugin(Object target) {
            return Plugin.wrap(target, this);
        }

        @Override
        public void setProperties(Properties properties) {
            // 可以通过 properties 配置拦截器参数
        }
    }
}
