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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import tk.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author LiYun
 */
@Configuration
@Slf4j
@MapperScan(basePackages = "org.changsha.changshapoc.dal.Mapper",sqlSessionFactoryRef = "primarySqlSessionFactory",sqlSessionTemplateRef = "primarySqlSessionTemplate")
public class PrimaryDataSourceConfig {

    @Value("${primary.datasource.url}")
    String mysqlUrl;

    @Value("${primary.datasource.username}")
    String mysqlUsername;

    @Value("${primary.datasource.password}")
    String mysqlPassword;

    @Value("${app.mock}")
    boolean isMock;

    @Bean(name = "primaryMysqlDataSource")
    @Primary
    public DataSource primaryMysqlDataSource() throws SQLException {
        if(isMock){
            return new DruidDataSource();
        }
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
        log.info("primary数据库初始化完毕============================");
        return ds;
    }

    @Bean(name = "primaryTransactionManager")
    @Primary
    public DataSourceTransactionManager primaryTransactionManager() throws SQLException {
        return new DataSourceTransactionManager(primaryMysqlDataSource());
    }

    @Bean(name = "primarySqlSessionFactory")
    @Primary
    public SqlSessionFactory primarySqlSessionFactory(@Qualifier("primaryMysqlDataSource") DataSource primaryDataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(primaryDataSource);
        return sessionFactory.getObject();
    }

    @Bean("primarySqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("primaryMysqlDataSource") DataSource dataSource) throws Exception {
        return new SqlSessionTemplate(primarySqlSessionFactory(dataSource));
    }
}