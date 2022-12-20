package com.exam.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
//@Configuration
@RequiredArgsConstructor
public class DbJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("잡이름")
                .start(step1())
                .next(step2())
                .build();
    }

    private Step step1() {
        return stepBuilderFactory.get("스텝1")
                .tasklet((contribution, chunkContext) -> {
                    log.info("스텝1 실행" );
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    private Step step2() {
        return stepBuilderFactory.get("스텝2")
                .tasklet((contribution, chunkContext) -> {
                    log.info("스텝2 실행" );
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
