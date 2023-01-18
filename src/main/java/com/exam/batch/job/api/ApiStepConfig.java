package com.exam.batch.job.api;

import com.exam.batch.domain.ProductVO;
import com.exam.batch.partitioner.ProductPartitioner;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class ApiStepConfig {

    private static final String PREFIX = "apiStep";

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private static int CHUNK_SIZE = 10;

    @Bean
    public Step apiMasterStep() {
        return stepBuilderFactory.get("apiMasterStep")
                .partitioner(apiSlaveStep().getName(), partitioner())
                .step(apiSlaveStep())
                .gridSize(3)
                .taskExecutor(taskExecutor())
                .build();
    }

    private TaskExecutor taskExecutor() {
        return null;
    }

    // 3개의 쓰레드각 각각 read, writer, prosseor 를 가짐
    @Bean
    public Step apiSlaveStep() {
        return stepBuilderFactory.get("apiSlaveStep")
                .<ProductVO, ProductVO>chunk(CHUNK_SIZE)
                .reader(itemReader(null))
                .processor(itemPocessor())
                .writer(itemWriter())
                .build();
    }

    // stepExecutionContext는 쓰레드마다 가지고 있으므로 멀티쓰레드에 안전하다
    @Bean(PREFIX + "ItemReader")
    @StepScope
    public ItemReader<? extends ProductVO> itemReader(@Value("#{stepExecutionContext['product']}") ProductVO productVO) {
        JdbcPagingItemReader<ProductVO> reader = new JdbcPagingItemReader<>(); // jdbc 페이징 리더사용

        reader.setDataSource(dataSource); // 데이터 소스세팅
        reader.setPageSize(CHUNK_SIZE); // 1회 조회 페이징수 설정, 청크사이즈와 동일해야 효율이 좋다
        reader.setRowMapper(new BeanPropertyRowMapper<>(ProductVO.class)); // 조회 결과 를 객체에 바인딩 처리하는 클래스

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider(); // mysql용 쿼리생성기 =
        queryProvider.setSelectClause("id, name, price, type"); // 조회 걸럼
        queryProvider.setFromClause("from product");
        queryProvider.setFromClause("where type = :type");

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("id", Order.DESCENDING);
        queryProvider.setSortKeys(sortKey);

        reader.setParameterValues(QueryGenerator.getParameterForQuery("type", productVO.getType()));

        return null;
    }

    private Function<? super ProductVO, ? extends ProductVO> itemPocessor() {
        return null;
    }

    private ItemWriter<? super ProductVO> itemWriter() {
        return null;
    }

    @Bean(PREFIX + "Partitioner")
    public ProductPartitioner partitioner() {
        return new ProductPartitioner(dataSource);
    }
}
