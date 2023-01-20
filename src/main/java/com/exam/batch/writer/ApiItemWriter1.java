package com.exam.batch.writer;

import com.exam.batch.domain.ApiRequestVO;
import com.exam.batch.domain.ApiResponseVO;
import com.exam.batch.service.AbstractApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ApiItemWriter1 extends FlatFileItemWriter<ApiRequestVO> {

    private final AbstractApiService abstractApiService;

    @Override
    public void write(List<? extends ApiRequestVO> items) throws Exception {
        log.info("write1 접근");
        ApiResponseVO responseVO = abstractApiService.service(items);
        log.info(responseVO.toString());

        items.forEach(item -> item.setApiResponseVO(responseVO));

        super.setResource(new FileSystemResource("D:\\workspace-my\\spring-batch\\src\\main\\resources\\product1.txt"));
        super.open(new ExecutionContext());
        super.setLineAggregator(new DelimitedLineAggregator<>()); // , 구분자로
        super.setAppendAllowed(true); // 응답값 계속추가
        super.write(items);
    }
}
