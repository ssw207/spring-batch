package com.exam.batch;

import com.exam.batch.entity.Pay;
import com.exam.batch.job.simple.SimpleJobConfiguration;
import com.exam.batch.job.simple.SimpleJobTasklet;
import com.exam.batch.repository.PayRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SpringBatchTest 어노테이션 자동등록 Bean
 *
 * - JobLauncherTestUtils
 * 스프링 배치 테스트에 필요한 전반적인 유틸 기능들을 지원
 *
 * - JobRepositoryTestUtils
 * DB에 생성된 JobExecution을 쉽게 생성/삭제 가능하게 지원
 *
 * - StepScopeTestExecutionListener
 * 배치 단위 테스트시 StepScope 컨텍스트를 생성
 * 해당 컨텍스트를 통해 JobParameter등을 단위 테스트에서 DI 받을 수 있음
 *
 * - JobScopeTestExecutionListener
 * 배치 단위 테스트시 JobScope 컨텍스트를 생성
 * 해당 컨텍스트를 통해 JobParameter등을 단위 테스트에서 DI 받을 수 있음
 */
//@SpringBatchTest // TODO 적용시 테스트 관련한 Bean을 등록 해준다고 하는데 jobLauncherTestUtils Bean이 생성되지 않음 확인필요
@SpringBootTest(classes = {SimpleJobConfiguration.class, SimpleJobTasklet.class, TestBatchConfig.class}) // 배치테스트옹 설정파일과 테스트할 job파일을 읽는다
public class BatchIntegrationTestJobConfigurationTest {

    @Autowired
    private PayRepository payRepository;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void 배치_실행_테스트() throws Exception {
        //given
        Pay pay = new Pay();
        pay.setAmount(100L);
        pay.setTxName("pay1");
        Pay saved = payRepository.save(pay);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestDate", "2021-01-01")
                .toJobParameters();

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        Pay find = payRepository.findById(saved.getId()).orElseThrow(() -> new IllegalArgumentException("조회실패"));
        assertThat(find.getAmount()).isEqualTo(100L);
        assertThat(find.getTxName()).isEqualTo("pay1");
    }
}
