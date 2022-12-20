package com.exam.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.desktop.PrintFilesEvent;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("잡이름")
                .start(step1())
                .next(step2())
                .build();
    }



    private Step step1() {
        return stepBuilderFactory.get("스탭")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        log.info("hello 스프링배치 1" );
                        return RepeatStatus.FINISHED; // 기본적으로 무한반복인데, FINISH를 리턴하면 종료한다
                    }
                })
                .build();
    }

    private Step step2() {
        return stepBuilderFactory.get("스탭")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        log.info("hello 스프링배치 2" );
                        return RepeatStatus.FINISHED; // 기본적으로 무한반복인데, FINISH를 리턴하면 종료한다
                    }
                })
                .build();
    }
}
