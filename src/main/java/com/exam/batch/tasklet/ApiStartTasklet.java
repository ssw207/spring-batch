package com.exam.batch.tasklet;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiStartTasklet implements Tasklet {

    private final JobExplorer jobExplorer; // 배치 메타데이터를 사용함

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // TODO tasklet에서 체크하면 이미 job이 실행된 상태이기 떄문에 현재 파라미터가 메타데이터에 저장되서 항상 실패한다. job바깥으로 이동해야됨
        validateParameter(contribution, "reqYmd");


        log.info("Api 서비스 시작");

        return RepeatStatus.FINISHED;
    }

    private void validateParameter(StepContribution contribution, String paramName) throws JobExecutionException {
        String jobName = getJobName(contribution);
        int jobInstanceCount = getJobInstanceCount(jobName);
        String jobParameter = getJobParameter(contribution, paramName);
        List<JobInstance> jobInstances = getJobInstances(jobName, jobInstanceCount);

        boolean alreadyExists = isAlreadyExistsParameter(paramName, jobParameter, jobInstances);

        if (alreadyExists) {
            throw new JobExecutionException(jobParameter + " already exists");
        }
    }

    private List<JobInstance> getJobInstances(String jobName, int jobInstanceCount) {
        log.info("jobName : {}, job count: {}", jobName, jobInstanceCount);
        return jobExplorer.getJobInstances(jobName, 0, jobInstanceCount);
    }

    private int getJobInstanceCount(String jobName) throws NoSuchJobException {
        return jobExplorer.getJobInstanceCount(jobName);
    }

    private String getJobName(StepContribution contribution) {
        return contribution.getStepExecution().getJobExecution().getJobInstance().getJobName();
    }

    private String getJobParameter(StepContribution contribution, String paramName) {
        return contribution.getStepExecution().getJobExecution().getJobParameters().getString(paramName);
    }

    private boolean isAlreadyExistsParameter(String paramName, String requestDate, List<JobInstance> jobInstances) {
        return jobInstances.stream() // job이름에 해당되는 instnace를 가져온다
                .flatMap(jobInstance -> jobExplorer.getJobExecutions(jobInstance).stream())
                .map(jobExecution -> jobExecution.getJobParameters().getString(paramName))
                .filter(StringUtils::isNotBlank)
                .anyMatch(savedParameter -> savedParameter.equals(requestDate));
    }
}
