package com.exam.batch.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class ApiRequestVO {

    private Long id;
    private ProductVO productVO;

}
