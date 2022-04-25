package com.exam.batch.job.reader;

import com.exam.batch.entity.Pay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcCursorItemReaderJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job jdbcCursorItemReaderJob() {
        return jobBuilderFactory.get("jdbcCursorItemReaderJob")
                .start(jdbcCursorItemReaderStep())
                .build();
    }

    @Bean
    public Step jdbcCursorItemReaderStep() {
        return stepBuilderFactory.get("jdbcCursorItemReaderStep")
                .<Pay, Pay>chunk(CHUNK_SIZE) // <Reader에서 반환될타입, Writer에 파라미터로 넘어올타입>, CHUNK_SIZE = Chunk 트렌젝션 범위
                .reader(jdbcCursorItemReader())
                .writer(jdbcCursorItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Pay> jdbcCursorItemReader() {
        return new JdbcCursorItemReaderBuilder<Pay>()
                .fetchSize(CHUNK_SIZE) // DB에서 한번에 조회할 데이터의 양 (Paging은 실제쿼리를 분할처리해 DB에서 가져오지만, Cursor는 실행은 분할처리 없이 조회함?)
                .dataSource(dataSource) // Cursor는 하나의 커넥션으로 배치가 끝날때까지 사용하기 때문에 배치 처리시간이 길면 커넥션이 끊어질수 있다. 오래걸리면다면 PagingItemReader를 사용할것.
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class)) // 쿼리결과를 맵핑
                .sql("SELECT id, amount, tx_name, tx_date_time From pay")
                .name("jdbcCursorItemReader") // Spring Batch에 ExecutionContext 에 저장될 이름
                .build();
    }

    private ItemWriter<Pay> jdbcCursorItemWriter() {
        return list -> {
            list.stream()
                    .forEach(pay -> log.info("Current Pay={}", pay));
        };
    }
}
