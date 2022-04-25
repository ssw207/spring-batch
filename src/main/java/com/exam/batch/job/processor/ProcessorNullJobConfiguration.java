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
public class ProcessorNullJobConfiguration {

    public static final String JOB_NAME = "processorNullBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${chunkSize:1000}")
    private int CHUNK_SIZE;

    @Bean(JOB_NAME)
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .preventRestart() // 실패했더라도 재시작하지 않는 플래그
                .start(step())
                .build();
    }

    @Bean(BEAN_PREFIX + "step")
    @JobScope
    public Step step() {
        return stepBuilderFactory.get(BEAN_PREFIX + "step")
                .<Pay, Pay>chunk(CHUNK_SIZE)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean(BEAN_PREFIX + "reader")
    public ItemReader<? extends Pay> reader() {
        return new JpaPagingItemReaderBuilder<Pay>()
                .name(BEAN_PREFIX + "reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT p FROM Pay p")
                .build();
    }

    @Bean(BEAN_PREFIX + "processor")
    public ItemProcessor<? super Pay, ? extends Pay> processor() {
        return pay -> {
            boolean isIgnoreTarget = pay.getId() % 2 == 0L;
            if (isIgnoreTarget) {
                log.info(">>>>> Pay name={}, isIgnoreTarget={}", pay.getTxName(), isIgnoreTarget);
                return null;
            }

            return pay;
        };
    }

    @Bean(BEAN_PREFIX + "writer")
    public ItemWriter<? super Pay> writer() {
        return items -> {
            items.stream()
                    .forEach(item -> log.info("item.getTxName={}", item.getTxName()));
        };
    }
}
