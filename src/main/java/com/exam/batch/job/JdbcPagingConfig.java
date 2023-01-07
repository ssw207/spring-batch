package com.exam.batch.job;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.exam.batch.domain.Product;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JdbcPagingConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    
    @Bean
    public Job jdbcPagingJob() throws Exception {
        return jobBuilderFactory.get("jdbcPagingJob")
                .start(jdbcPagingStep())
                .build();
    }

    @Bean
    public Step jdbcPagingStep() throws Exception {
        return stepBuilderFactory.get("jdbcPagingStep")
                .<Product, Product>chunk(10)
                .reader(jdbcPagingReader())
                .writer(jdbcPagingWriter())
                .build();
    }

    @Bean
    public ItemReader<Product> jdbcPagingReader() throws Exception {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("price", 10);

        return new JdbcPagingItemReaderBuilder<Product>()
            .name("jdbcPagingReader")
            .pageSize(10)
            .dataSource(dataSource)
            .rowMapper(new BeanPropertyRowMapper<>(Product.class))
            .queryProvider(createQueryProvider())
            .parameterValues(parameterValues)
            .build();
    }

    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("id, name,price");
        queryProvider.setFromClause("from product");
        queryProvider.setWhereClause("where price >= :price");
        queryProvider.setSortKey("id");

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }

    @Bean
    public ItemWriter<Object> jdbcPagingWriter() {
        return items -> {
            items.forEach(System.out::println);
        };
    }
}
