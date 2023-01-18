package com.exam.batch.job.api;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ApiJobChildConfig {

    private static final String PREFIX = "apiChild";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final Step apiMasterStep;
    private final Step apiSlaveStep;
    private final JobLauncher jobLauncher;

    @Bean(PREFIX + "Step")
    public Step step() { // jop을 실행하는 step
        return stepBuilderFactory.get(PREFIX + "Step")
                .job(job())
                .launcher(jobLauncher)
                .build();
    }

    @Bean(PREFIX + "Job")
    public Job job() {
        return jobBuilderFactory.get(PREFIX + "Job")
                .start(apiMasterStep)
                .build();

    }
}
