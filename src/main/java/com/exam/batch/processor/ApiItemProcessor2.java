package com.exam.batch.processor;

import com.exam.batch.domain.ApiRequestVO;
import com.exam.batch.domain.ProductVO;
import org.springframework.batch.item.ItemProcessor;

// type이 2인 데이터가 전달된다
public class ApiItemProcessor2 implements ItemProcessor<ProductVO, ApiRequestVO> {

    @Override
    public ApiRequestVO process(ProductVO item) throws Exception {
        return ApiRequestVO.builder()
                .id(item.getId())
                .productVO(item)
                .build();
    }
}
