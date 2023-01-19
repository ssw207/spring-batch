package com.exam.batch.writer;

import com.exam.batch.domain.ApiRequestVO;
import com.exam.batch.service.AbstractApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
public class ApiItemWriter1 implements ItemWriter<ApiRequestVO> {

    private final AbstractApiService abstractApiService;

    @Override
    public void write(List<? extends ApiRequestVO> items) throws Exception {

    }
}
