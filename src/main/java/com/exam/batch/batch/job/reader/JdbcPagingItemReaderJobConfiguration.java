package com.exam.batch.batch.job.reader;

import com.exam.batch.batch.entity.Pay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcPagingItemReaderJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    private final int CHUNK_SIZE = 10;

    @Bean
    public Job jdbcPagingItemReaderJob() throws Exception {
        return jobBuilderFactory.get("jdbcPagingItemReaderJob")
                .start(jdbcPagingItemReaderStep())
                .build();
    }

    @Bean
    public Step jdbcPagingItemReaderStep() throws Exception {
        return stepBuilderFactory.get("jdbcPagingItemReaderStep")
                .<Pay, Pay>chunk(CHUNK_SIZE)
                .reader(jdbcPagingItemReader()) // reader실행
                .writer(jdbcPagingItemWriter())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Pay> jdbcPagingItemReader() throws Exception {
        //쿼리파라미터 정의
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("amount", 2000);

        return new JdbcPagingItemReaderBuilder<Pay>()
                .pageSize(CHUNK_SIZE) // 페이징 사이즈
                .fetchSize(CHUNK_SIZE) // ?
                .dataSource(dataSource) // DB정보 전달
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class)) // 조회결과를 어떤클래스에 맵핑할건지
                .queryProvider(createQueryProvider()) // 조회 쿼리생성
                .parameterValues(parameterValues) // 조회 쿼리에 전달할 파라미터
                .name("jdbcPagingItemReader") // 메타데이터에 저장될 이름
                .build();
    }

    private ItemWriter<Pay> jdbcPagingItemWriter() {
        return list -> {
            list.stream()
                    .forEach(pay -> log.info("Current Pay ={}", pay));
        };
    }

    private PagingQueryProvider createQueryProvider() throws Exception {

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource); //Data 설정값을 보고 DB Provider를 자동으로 선택함 (페이징 쿼리를 DB맞춰 생성)
        queryProvider.setSelectClause("id, amount, tx_name, tx_date_time"); // 조회컬럼
        queryProvider.setFromClause("from pay"); // 테이블
        queryProvider.setWhereClause("where amount >= :amount"); // :amount는 parameterValues에 key값으로 확인함

        //정렬
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }
}
