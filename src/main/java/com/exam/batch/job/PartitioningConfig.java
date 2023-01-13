package com.exam.batch.job;

import com.exam.batch.domain.Book;
import com.exam.batch.domain.BookShop;
import com.exam.batch.listener.StopWatchJobListener;
import com.exam.batch.partitioner.ColumnRangePartitioner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class PartitioningConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final EntityManagerFactory emf;
    private final EntityManager em;

    @Bean
    public Job partitioningJob() {
        return jobBuilderFactory.get("partitioningJob")
                .incrementer(new RunIdIncrementer())
                .start(masterStep())
                .listener(new StopWatchJobListener())// job 리스너 총실행시간을 측정한다
                .build();
    }

    @Bean
    public Step masterStep() {
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep().getName(), partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Partitioner partitioner() {
        ColumnRangePartitioner partitioner = new ColumnRangePartitioner();
        partitioner.setColumn("id");
        partitioner.setEntityName("Book");
        partitioner.setEntityManager(em);
        return partitioner;
    }

    @Bean
    public Step slaveStep() {
        return stepBuilderFactory.get("slaveStep")
                .<Book, BookShop>chunk(5)
                .reader(partitioningItemReader(null, null))
                .writer(jpaPagingWriter())
                .build();

    }

    public ItemWriter<BookShop> jpaPagingWriter() {
        return new JpaItemWriterBuilder<BookShop>()
                .usePersist(true)
                .entityManagerFactory(emf)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Book> partitioningItemReader(
            @Value("#{stepExecutionContext['min']}") Long min,
            @Value("#{stepExecutionContext['max']}") Long max
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put("min", min);
        map.put("max", max);

        log.info(map.toString());

        return new JpaPagingItemReaderBuilder<Book>()
                .name("partitioningItemReader")
                .pageSize(5)
                .entityManagerFactory(emf)
                .queryString("select p from Book p where p.id >= :min and p.id < :max")
                .parameterValues(map)
                .build();
    }

    @Bean
    public TaskExecutor partitioningTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4); // 최초 풀 초기화시 4개 쓰레드생성
        taskExecutor.setMaxPoolSize(8); // 큐에 요청이 가득차면 쓰레드를 최대 8개까지 생성
        taskExecutor.setThreadNamePrefix("async-Thread");
        return taskExecutor;
    }
}
