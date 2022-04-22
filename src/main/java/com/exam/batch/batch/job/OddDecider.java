package com.exam.batch.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.util.Random;

@Slf4j
public class OddDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        Random random = new Random();

        int randomNumber = random.nextInt(50) + 1;
        log.info("랜덤숫자: {}", randomNumber);

        if (randomNumber % 2 == 0) {
            return new FlowExecutionStatus("EVEN"); // FlowExecutionStatus Step으로 처리하는게 아니기때문
        } else {
            return new FlowExecutionStatus("ODD");
        }
    }
}
