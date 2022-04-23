package com.exam.batch.batch.job.simple;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope // 스탭 생성시 Bean이 생성되면서
@Component
public class SimpleJobTasklet implements Tasklet {

    @Value("#{jobParameters[requestDate]}") // 외부에서 Bean생성시 주입됨 (Step 생성시)
    private String requestDate;

    public SimpleJobTasklet() {
        log.info("SimpleJobTasklet 생성");
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info(">>>>> Step1");
        log.info(">>>>> requestDate :{}", requestDate);
        return RepeatStatus.FINISHED;
    }
}
