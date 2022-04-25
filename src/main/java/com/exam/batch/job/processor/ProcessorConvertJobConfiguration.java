package com.exam.batch.job.processor;

import com.exam.batch.entity.Pay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ProcessorConvertJobConfiguration {

    public static final String JOB_NAME = "ProcessorConvertBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${chunkSize:1000}") // 프로퍼티에 chunkSize 값이 없으면 1000 할당
    private int CHUNK_SIZE;

    @Bean(JOB_NAME) // bean의 이름을 지정함. 디폴트는 메소드이름
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .preventRestart() // 이건왜하는거지?
                .start(step())
                .build();
    }

    @Bean(BEAN_PREFIX + "step")
    @JobScope // job이 생성될때 생성됨
    public Step step() {
        return stepBuilderFactory.get(BEAN_PREFIX + "step")
                .<Pay, String>chunk(CHUNK_SIZE)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public ItemReader<Pay> reader() {
        return new JpaPagingItemReaderBuilder<Pay>()
                .name(BEAN_PREFIX + "reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT p FROM Pay p")
                .build();
    }

    private ItemProcessor<Pay, String> processor() {
        return pay -> pay.getTxName();
    }

    private ItemWriter<String> writer() {
        return items -> {
            items.stream()
                    .forEach(item -> log.info("item={}", item));
        };
    }
}
