package com.exam.batch.writer;

import com.exam.batch.domain.ApiRequestVO;
import com.exam.batch.service.AbstractApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ApiItemWriter1 implements ItemWriter<ApiRequestVO> {

    private final AbstractApiService abstractApiService;

    @Override
    public void write(List<? extends ApiRequestVO> items) throws Exception {
        log.info("write1 접근");
        abstractApiService.service(items);
    }
}
