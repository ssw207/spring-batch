package com.exam.batch.job.test;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

/**
 * @SpringBootTest가 자동으로 해주는 작업을 수동으로 처리함
 * 스프링의존성을 사용하지 않으므로 속도상 장점이 있다.
 */
@Configuration
public class TestDataSourceConfiguration {

    //Reader 쿼리가 수행될 테이블 생성
    private static final String CREATE_SQL = "create table IF NOT EXISTS 'sales' (id bigint not null auto_increment, amount bigint not null, order_date date, order_no varchar(255), primary key (id)) engine=InnoDB;";

    //테스트용 db실행
    @Bean
    public DataSource dataSource() {
        EmbeddedDatabaseFactory databaseFactory = new EmbeddedDatabaseFactory();
        databaseFactory.setDatabaseType(H2);
        return databaseFactory.getDatabase();
    }

    //db초기 작업 실행
    @Bean
    public DataSourceInitializer initializer(DataSource dataSource) {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);

        //테이블생성 쿼리 실행
        Resource create = new ByteArrayResource(CREATE_SQL.getBytes(StandardCharsets.UTF_8));
        dataSourceInitializer.setDatabasePopulator(new ResourceDatabasePopulator(create));

        return dataSourceInitializer;
    }
}
