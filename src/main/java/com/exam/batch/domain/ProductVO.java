package com.exam.batch.domain;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor // 파일에서 읽은정보를 매핑하려면 디폴트 생성자가 반드시 필요함
public class ProductVO {

    private Long id;
    private String name;
    private Integer price;
    private String type;

    @Builder
    public ProductVO(Long id, String name, Integer price, String type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.type = type;
    }
}
