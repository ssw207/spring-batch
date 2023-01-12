package com.exam.batch.job;

import com.exam.batch.domain.Customer;
import com.exam.batch.domain.Product;
import com.exam.batch.listener.CustomItemProcessorListener;
import com.exam.batch.listener.CustomItemReadListener;
import com.exam.batch.listener.CustomItemWiterListener;
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
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultiThreadJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    @Bean
    public Job multiThreadJob() {
        return jobBuilderFactory.get("multiThreadJob")
                .start(multiThreadStep())
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean
    public Step multiThreadStep() {
        return stepBuilderFactory.get("multiThreadStep")
                .<Product, Customer>chunk(100)
                .reader(testJpaPagingReader2())
                .listener(new CustomItemReadListener())
                .processor(custItemProcessor2())
                .listener(new CustomItemProcessorListener())
                .writer(customItemWriter2())
                .listener(new CustomItemWiterListener())
                .taskExecutor(taskExecutor()) // 추가만하면 자동으로 비동기 실행을한다
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(8);
        taskExecutor.setThreadNamePrefix("async-thread");
        return taskExecutor;
    }

    @Bean
    public ItemProcessor<Product, Customer> custItemProcessor2() {
        return item -> {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Customer(item.getId(), item.getName().toLowerCase(), item.getPrice() * 1000);
        };
    }

    private ItemWriter<Customer> customItemWriter2() {
        return new JpaItemWriterBuilder<Customer>()
                .usePersist(true)
                .entityManagerFactory(emf)
                .build();
    }

    @Bean
    public ItemProcessor multiThreadProcessor() {
        AsyncItemProcessor<Product, Customer> processor = new AsyncItemProcessor<>();
        processor.setDelegate(custItemProcessor2()); // 비동기로 위임
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return processor;
    }

    @Bean
    public ItemReader<Product> testJpaPagingReader2() {
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
    public ItemWriter multiThreadItemWriter() {

        AsyncItemWriter<Customer> writer = new AsyncItemWriter<>();
        writer.setDelegate(customItemWriter2());

        return writer;
    }
}
