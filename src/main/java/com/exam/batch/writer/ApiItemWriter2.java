package com.exam.batch.writer;

import com.exam.batch.domain.ApiRequestVO;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class ApiItemWriter2 implements ItemWriter<ApiRequestVO> {

    @Override
    public void write(List<? extends ApiRequestVO> items) throws Exception {

    }
}
