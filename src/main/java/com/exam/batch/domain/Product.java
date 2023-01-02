package com.exam.batch.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@ToString
@Getter
@Setter
@Entity
public class Product {

    @Id
    private String id;
    private String name;
    private int price;
}
