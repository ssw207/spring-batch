package com.exam.batch.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class ProductVO {

    private Long id;
    private String name;
    private Integer price;
    private String type;
}
