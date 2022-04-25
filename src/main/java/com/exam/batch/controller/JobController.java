package com.exam.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class JobController {

    private final JobLauncher jobLauncher;
    //private final List<Job> job; // job타입으로 등록된 bean을 List로 받는경우
    private final ApplicationContext applicationContext; // 직접 스프링컨테이너에서 bean 꺼내는 경우

    @GetMapping("/run")
    public String handle(String requestDate) {

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                                            .addString("requestDate", requestDate) // jobPameter 생성
                                            .addLong("time", System.currentTimeMillis())
                                            .toJobParameters();

            Job simpleJob1 = applicationContext.getBean("simpleJob1", Job.class);

            //job.stream().forEach(bean -> log.info("bean.getName() {}", bean.getName()));

            jobLauncher.run(simpleJob1, jobParameters); // job실행
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        return "DONE";
    }
}
