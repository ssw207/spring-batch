package com.exam.batch.batch.job.simple;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;

/**
 * Skip 횟수를 판단해 커스텀 ExitStatus를 리턴하는 클래스
 */
public class SkipCheckingListener extends StepExecutionListenerSupport {

    public ExitStatus afterStep(StepExecution stepExecution) {
        String exitCode = stepExecution.getExitStatus().getExitCode();

        if (!exitCode.equals(ExitStatus.FAILED.getExitCode()) // Step 성공여부 확인
                && stepExecution.getSkipCount() > 0) { // Skip이 1이상이면
            return new ExitStatus("COMPLETED WITH SKIP"); // 커스텀 ExitStatus 상태값 리턴
        } else {
          return null;
        }
    }
}
