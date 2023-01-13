package com.exam.batch.job;

import com.exam.batch.listener.StopWatchJobListener;
import com.exam.batch.step.SumTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@RequiredArgsConstructor
@Configuration
public class ParallelStep2Config {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job parallelStepJob() {
        return jobBuilderFactory.get("parallelStepJob")
                .incrementer(new RunIdIncrementer())
                .start(flow1())
                .split(parallelTaskExecutor()).add(flow2()) // 쓰레드 풀로 flow1, flow2를 쓰레드를 실행함
                .end()// 플로우 종료
                .listener(new StopWatchJobListener())// job 리스너 총실행시간을 측정한다
                .build();
    }

    @Bean
    public Flow flow1() {

        TaskletStep step1 = stepBuilderFactory.get("parallelFlow1Step1")
                .tasklet(sumTasklet())
                .build();

        return new FlowBuilder<Flow>("flow1")
                .start(step1)
                .build();
    }

    @Bean
    public Flow flow2() {

        TaskletStep step2 = stepBuilderFactory.get("parallelFlow1Step2")
                .tasklet(sumTasklet())
                .build();

        TaskletStep step3 = stepBuilderFactory.get("parallelFlow1Step3")
                .tasklet(sumTasklet())
                .build();

        return new FlowBuilder<Flow>("flow2")
                .start(step2)
                .next(step3)
                .build();
    }

    @Bean
    public Tasklet sumTasklet() {
        return new SumTasklet();
    }


    @Bean
    public TaskExecutor parallelTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4); // 최초 풀 초기화시 4개 쓰레드생성
        taskExecutor.setMaxPoolSize(8); // 큐에 요청이 가득차면 쓰레드를 최대 8개까지 생성
        taskExecutor.setThreadNamePrefix("async-Thread");
        return taskExecutor;
    }
}
