package com.exam.batch.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
public class Product2 {

    @Id
    private Long id;
    private String name;
    private Integer price;
    private String type;
}
