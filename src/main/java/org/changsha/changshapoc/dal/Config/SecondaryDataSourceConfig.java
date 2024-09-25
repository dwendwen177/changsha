/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package org.changsha.changshapoc.dal.Config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@Slf4j
@MapperScan(basePackages = "org.changsha.changshapoc.dal.Mapper.Secondary",sqlSessionFactoryRef = "secondarySqlSessionFactory",sqlSessionTemplateRef = "secondarySqlSessionTemplate")
public class SecondaryDataSourceConfig {

    @Value("${secondary.datasource.url}")
    String mysqlUrl;

    @Value("${secondary.datasource.username}")
    String mysqlUsername;

    @Value("${secondary.datasource.password}")
    String mysqlPassword;

    @Bean(name = "secondaryMysqlDataSource")
    public DataSource secondaryMysqlDataSource() throws SQLException {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(mysqlUrl);
        ds.setUsername(mysqlUsername);
        ds.setPassword(mysqlPassword);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setInitialSize(10);
        ds.setMinIdle(5);
        ds.setMaxActive(100);
        ds.setMaxWait(60000);
        ds.setPoolPreparedStatements(false);
        ds.setDefaultAutoCommit(true);
        ds.setValidationQuery("select 1");
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);
        ds.setTimeBetweenEvictionRunsMillis(180000);
        ds.setMinEvictableIdleTimeMillis(3600000);
        ds.setRemoveAbandoned(true);
        ds.setRemoveAbandonedTimeout(300);
        ds.init();
        log.info("secondary数据库初始化完毕============================");
        return ds;
    }

    @Bean(name = "secondaryTransactionManager")
    public DataSourceTransactionManager secondaryTransactionManager() throws SQLException {
        return new DataSourceTransactionManager(secondaryMysqlDataSource());
    }

    @Bean(name = "secondarySqlSessionFactory")
    public SqlSessionFactory secondarySqlSessionFactory(@Qualifier("secondaryMysqlDataSource") DataSource dataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        return sessionFactory.getObject();
    }

    @Bean("secondarySqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("secondaryMysqlDataSource") DataSource dataSource) throws Exception {
        return new SqlSessionTemplate(secondarySqlSessionFactory(dataSource));
    }
}