package com.exam.batch.batch;

import org.junit.runner.RunWith;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @SpringBatchTest 어노테이션 자동등록 Bean
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
@RunWith(SpringRunner.class)
@SpringBatchTest
public class BatchIntegrationTestJobConfigurationLegacyTest {

}
