package com.exam.batch.job.api;

import com.exam.batch.domain.ApiRequestVO;
import com.exam.batch.domain.ProductVO;
import com.exam.batch.job.classifier.ProcessorClassifier;
import com.exam.batch.job.classifier.WriterClassifier;
import com.exam.batch.partitioner.ProductPartitioner;
import com.exam.batch.processor.ApiItemProcessor1;
import com.exam.batch.processor.ApiItemProcessor2;
import com.exam.batch.processor.ApiItemProcessor3;
import com.exam.batch.service.ApiService1;
import com.exam.batch.service.ApiService2;
import com.exam.batch.writer.ApiItemWriter1;
import com.exam.batch.writer.ApiItemWriter2;
import com.exam.batch.writer.ApiItemWriter3;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


/**
 * 실제 비즈니스 로직을 수행하는 Step 설정 클래스
 * 파티셔너를 통해 읽어온 row를 type값을 기준으로 분류해 나누고 멀티 쓰레드로 처리한다
 * 한번에 N개 데이터를 읽어오로 N개 데이터를 3개의 쓰레드로 나눠서 처리함.
 */
@Configuration
@RequiredArgsConstructor
public class ApiStepConfig {

    private static final String PREFIX = "apiStep";
    private static int CHUNK_SIZE = 10;

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final ApiService1 apiService1;
    private final ApiService2 apiService2;
    private final ApiService2 apiService3;

    @Bean
    public Step apiMasterStep() {
        return stepBuilderFactory.get("apiMasterStep")
                .partitioner(apiSlaveStep().getName(), partitioner()) // 파티셔너 설정
                .step(apiSlaveStep()) // 실제 비즈니스를 처리하는 스탭. 각 스탭은 자신만의 stepExcution을 가진다
                .gridSize(3) // 처리할 쓰레드수. 쓰레드 수에 맞춰 스탭이 복제된다.
                .taskExecutor(taskExecutor()) // 멀티쓰레드를 처리할 쓰레드풀
                .build();
    }

    // 3개의 쓰레드각 각각 read, writer, prosseor 를 가짐

    @Bean
    public Step apiSlaveStep() {
        return stepBuilderFactory.get("apiSlaveStep")
                .<ProductVO, ProductVO>chunk(CHUNK_SIZE) // <입력, 출력> 타입.
                .reader(itemReader(null)) // 파라미터는 DI로 Step 생성시점에 주입된다.
                .processor(itemProcessor()) // type에 따라 아이테프로세서를 선택해호출한다
                .writer(itemWriter()) // type에 따라 아이테프로세서를 선택해호출한다
                .build();
    }
    // stepExecutionContext는 쓰레드마다 가지고 있으므로 멀티쓰레드에 안전하다
    // TODO 스탭익스큐션에 클래스도 넣을수 있나?

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
        queryProvider.setWhereClause("where type = :type");

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("id", Order.DESCENDING);
        queryProvider.setSortKeys(sortKey); // 정렬조건 세팅

        // 조회시 사용하는 파라미터
        reader.setQueryProvider(queryProvider);
        reader.setParameterValues(QueryGenerator.getParameterForQuery("type", productVO.getType()));

        return reader;
    }


    @Bean(PREFIX + "ItemProcessor")
    public ItemProcessor itemProcessor() {
        ProcessorClassifier classifier = new ProcessorClassifier();
        classifier.setProcessorMap(getItemProcessorMap());

        ClassifierCompositeItemProcessor<ProductVO, ApiRequestVO> processor = new ClassifierCompositeItemProcessor<>();
        processor.setClassifier(classifier);
        return processor;
    }

    private Map<String, ItemProcessor<ProductVO, ApiRequestVO>> getItemProcessorMap() {
        Map<String, ItemProcessor<ProductVO, ApiRequestVO>> processorMap = new HashMap<>();
        processorMap.put("1", new ApiItemProcessor1());
        processorMap.put("2", new ApiItemProcessor2());
        processorMap.put("3", new ApiItemProcessor3());
        return processorMap;
    }

    @Bean(PREFIX + "ItemWriter")
    public ItemWriter itemWriter() {
        WriterClassifier classifier = new WriterClassifier(getItemWriterMap());
        ClassifierCompositeItemWriter<ApiRequestVO> processor = new ClassifierCompositeItemWriter<>();
        processor.setClassifier(classifier);
        return processor;
    }

    private Map<String, ItemWriter<ApiRequestVO>> getItemWriterMap() {
        Map<String, ItemWriter<ApiRequestVO>> writerMap = new HashMap<>();
        
        writerMap.put("1", new ApiItemWriter1(apiService1));
        writerMap.put("2", new ApiItemWriter2(apiService2));
        writerMap.put("3", new ApiItemWriter3(apiService3));

        return writerMap;
    }

    @Bean(PREFIX + "Partitioner")
    public ProductPartitioner partitioner() {
        return new ProductPartitioner(dataSource);
    }

    @Bean(PREFIX + "TaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(3);
        taskExecutor.setCorePoolSize(6);
        taskExecutor.setThreadNamePrefix("api-thread");
        return taskExecutor;
    }
}

