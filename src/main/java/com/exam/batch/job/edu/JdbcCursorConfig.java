package com.exam.batch.job.edu;

import com.exam.batch.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcCursorConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;


    @Bean
    public Job jdbcCursorJob() {
        return jobBuilderFactory.get("jdbcCursorJob")
                .start(jdbcStep())
                .build();
    }

    private Step jdbcStep() {
        return null;
    }

    @Bean
    public Step jdbcCursorStep() {
        return stepBuilderFactory.get("jdbcCursorStep")
                .<Product, Product>chunk(10)
                .reader(jdbcCursorReader())
                .writer(jdbcWriter())
                .build();
    }

    private ItemReader<Product> jdbcCursorReader() {
        return new JdbcCursorItemReaderBuilder<Product>()
                .name("jdbcCursorReader")
                .fetchSize(10)
                .sql("select * from product")
                .beanRowMapper(Product.class)
                .queryArguments("A%")
                .dataSource(dataSource)
                .build();
    }


    @Bean
    public ItemWriter<Object> jdbcWriter() {
        return items -> {
            items.forEach(System.out::println);
        };
    }
}
