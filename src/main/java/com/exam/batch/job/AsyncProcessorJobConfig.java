package com.exam.batch.job;

import com.exam.batch.domain.Customer;
import com.exam.batch.domain.Product;
import com.exam.batch.listener.StopWatchJobListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AsyncProcessorJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    @Bean
    public Job asyncJob() {
        return jobBuilderFactory.get("asyncJob")
                .start(asyncStep())
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean
    public Step normalStep() {
        return stepBuilderFactory.get("normalStep")
                .<Product, Customer>chunk(100)
                .reader(testJpaPagingReader())
                .processor(custItemProcessor())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public Step asyncStep() {
        return stepBuilderFactory.get("asyncStep")
                .<Product, Customer>chunk(100)
                .reader(testJpaPagingReader())
                .processor(asyncProcessor())
                .writer(awyncItemWriter())
                .build();
    }

    @Bean
    public ItemProcessor<Product, Customer> custItemProcessor() {
        return item -> {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Customer(item.getId(), item.getName().toLowerCase(), item.getPrice() * 1000);
        };
    }

    private ItemWriter<Customer> customItemWriter() {
        return new JpaItemWriterBuilder<Customer>()
                .usePersist(true)
                .entityManagerFactory(emf)
                .build();
    }

    @Bean
    public AsyncItemProcessor asyncProcessor() {
        AsyncItemProcessor<Product, Customer> processor = new AsyncItemProcessor<>();
        processor.setDelegate(custItemProcessor()); // 비동기로 위임
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        //processor.afterPropertiesSet(); bean으로 등록하지 않은경우 실행해야함

        return processor;
    }

    @Bean
    public ItemReader<Product> testJpaPagingReader() {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("price", 0);

        return new JpaPagingItemReaderBuilder<Product>()
                .name("jpaPagingReader")
                .pageSize(10)
                .entityManagerFactory(emf)
                .queryString("SELECT p FROM Product p where price >= :price")
                .parameterValues(parameterValues)
                .build();
    }

    @Bean
    public ItemWriter awyncItemWriter() {

        AsyncItemWriter<Customer> writer = new AsyncItemWriter<>();
        writer.setDelegate(customItemWriter());

        return writer;
    }
}
