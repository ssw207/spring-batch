package com.exam.batch.job.edu;

import com.exam.batch.domain.Customer;
import com.exam.batch.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class JdbcPagingConfig {

    private static final String PREFIX = "JdbcPagingConfig";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    @Bean(name = PREFIX + "Job")
    public Job job() throws Exception {
        return jobBuilderFactory.get(PREFIX + "Job")
                .start(step())
                .incrementer(getIncrementer())
                .build();
    }

    private JobParametersIncrementer getIncrementer() {
        return parameters -> {
            JobParameters jobParameters = Optional.ofNullable(parameters)
                    .orElse(new JobParameters());

            return new JobParametersBuilder(jobParameters)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
        };
    }

    @Bean(name = PREFIX + "Step")
    public Step step() throws Exception {
        return stepBuilderFactory.get(PREFIX + "Step")
                .<Product, Customer>chunk(10)
                .reader(reader())
                .writer(writer())
                .build();
    }

    @Bean(name = PREFIX + "Reader")
    public ItemReader<Product> reader() throws Exception {
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

    @Bean(name = PREFIX + "CreateQueryProvider")
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

    @Bean(name = PREFIX + "Writer")
    public ItemWriter<Customer> writer() {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                .sql("INSERT INTO CUSTOMER (ID, NAME) VALUES (:id, :name)")
                .beanMapped() // Customer의 필드명과 SQL의 파라미터명이 같아야 한다.
                .build();
    }
}
