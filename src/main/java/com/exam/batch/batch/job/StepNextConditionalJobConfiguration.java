package com.exam.batch.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * step의 흐름을 제어하는 배치 예제
 *
 * 시나리오
 * step1 실패  -> step3
 * step1 성공 -> step2 -> step3
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StepNextConditionalJobConfiguration {
    public final JobBuilderFactory jobBuilderFactory;
    public final StepBuilderFactory stepBuilderFactory;
    private String FAILED = "FAILED";

    @Bean
    public Job stepNextConditionalJob() {
        return jobBuilderFactory.get("stepNextConditionalJob")
                .start(conditionalStep1())// step1 시작
                    .on(FAILED) // step1이 실패하면
                    .to(conditionalStep3()) // step3으로 이동
                    .on("*") // step3 결과에 상관없으
                    .end()// step3으로 이동하면 Flow 종료
                .from(conditionalStep1()) // step1로 부터
                    .on("*")// step1 결과에 상관없이
                    .to(conditionalStep2())// step2로 이동
                    .next(conditionalStep3()) // step2가 정상 종료되면 step3으로 이동
                    .on("*")// step3의 결과와 상관없이
                    .end()// flow 종료
                .end() // job 종료
                .build();
    }

    @Bean
    public Step conditionalStep1() {
        return stepBuilderFactory.get("step1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>> step1");

                    /**
                     * ExitStatus를 FAILED로 지정한다.
                     * Flow는 이 값을 보고 진행됨
                     */
                    contribution.setExitStatus(ExitStatus.FAILED);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalStep2() {
        return stepBuilderFactory.get("step2")
                .tasklet((contribution, chunkContext) -> {
                    log.info("step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step conditionalStep3() {
        return stepBuilderFactory.get("step3")
                .tasklet((contribution, chunkContext) -> {
                    log.info("step3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
