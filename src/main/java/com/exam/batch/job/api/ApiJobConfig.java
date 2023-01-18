package com.exam.batch.job.api;

import com.exam.batch.listener.StopWatchJobListener;
import com.exam.batch.tasklet.ApiStartTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ApiJobConfig {

    private static final String PREFIX = "api";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ApiStartTasklet apiStartTasklet;
    private final ApiEndTasklet apiEndTaskelt;
    private Step jobStep;

    /**
     * API전체 통신 시작 - 종료등 요청만 실행
     * 실제 로직은 jobStep 에서 실행
     */
    @Bean(PREFIX + "Job")
    public Job job() {
        return jobBuilderFactory.get(PREFIX + "Job")
                .listener(new StopWatchJobListener())
                .start(apiStep1())
                .next(jobStep)
                .next(apiStep2())
                .build();


    }

    @Bean(PREFIX + "Step1")
    public Step apiStep1() {
        return stepBuilderFactory.get(PREFIX + "Step1")
                .tasklet(apiStartTasklet)
                .build();
    }

    @Bean(PREFIX + "Step2")
    public Step apiStep2() {
        return stepBuilderFactory.get(PREFIX + "Step2")
                .tasklet(apiEndTaskelt)
                .build();
    }
}
