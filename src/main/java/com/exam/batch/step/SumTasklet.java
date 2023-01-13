package com.exam.batch.step;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
public class SumTasklet implements Tasklet {


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        long sum = 0;
        synchronized (this) { // 락을걸어 쓰레드가 동시에 접근할수 없게 한다
            // 동기화 이슈가 발생한다 동시에 3개쓰레드가 sum++ 를 실행하면 3이 아니라 1이 증가함
            for (int i = 0; i < 1000000000; i++) {
                sum++;
            }

            log.info("{} 실행됨 실행 쓰레드 : {} / sum : {}",
                    chunkContext.getStepContext().getStepName(),
                    Thread.currentThread().getName(), sum);
        }


        return RepeatStatus.FINISHED;
    }
}
