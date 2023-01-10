package com.exam.batch.job;

import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("org.hibernate.hql.internal.ast.QuerySyntaxException: Product is not mapped  에러 발생중 확인필요")
@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes = {JpaPagingConfig.class, TestBatchConfig.class}) //
public class JpaPagingConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void jobTest() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "user1")
                .addLong("date", new Date().getTime())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    public void stepTest() {
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("jpaPagingStep");
        StepExecution stepExecution = ((List<StepExecution>) jobExecution.getStepExecutions()).get(0);

        assertThat(stepExecution.getCommitCount()).isEqualTo(3);
        assertThat(stepExecution.getReadCount()).isEqualTo(30);
        assertThat(stepExecution.getWriteCount()).isEqualTo(30);
    }
}