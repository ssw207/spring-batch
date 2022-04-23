package com.exam.batch.batch.job.simple;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final SimpleJobTasklet simpleJobTasklet;

    /**
     * Batch Job Bean등록
     * Job구조
     * - Step
     *   - Tasklet (@Bean과 비슷한 역할. 명확한 기능없지만 커스텀한 기능의 단위로 표현)
     *   - Reader, Processor, Writer   
     */
    @Bean
    public Job simpleJob1() {
        return jobBuilderFactory.get("simpleJob1") // simpleJob이름의 Batch Job 등록
                .start(simpleStep1()) // Job이 simpleStep1 Batch Step을 실행
                .next(simpleStep2(null))
                .build();
    }

//    @Bean
//    @JobScope
    public Step simpleStep1() {
        return stepBuilderFactory.get("simpleStep1")
                .tasklet(simpleJobTasklet) // Job이 Step에 진입할때 Bean을 생성해서 주입, 이때 jobParameter도 같이 주입한다.
                .build();
    }


    @Bean
    @JobScope
    public Step simpleStep2(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return stepBuilderFactory.get("simpleStep2") // simpleStep1이름의 Batch Step 등록
                // Step안에서 단일로 수행될 기능 명시
                .tasklet(((contribution, chunkContext) -> {
                    log.info(">>>>> simpleStep2 시작");
                    log.info(">>>>> 요청일시 : {}", requestDate);
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }
}
