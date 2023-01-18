package com.exam.batch.job.api;

import com.exam.batch.listener.StopWatchJobListener;
import com.exam.batch.tasklet.ApiEndTasklet;
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
public class ApiJobMasterConfig {

    private static final String PREFIX = "api";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ApiStartTasklet apiStartTasklet;
    private final ApiEndTasklet apiEndTasklet;
    private final Step jobStep;

    /**
     * API 관련 JOB을 총괄하는 클래스
     * 책임
     * - 배치 시작전 작업
     * - 실제 배치 처리 위임
     * - 배치 종료후 작업
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
                .tasklet(apiEndTasklet)
                .build();
    }
}
