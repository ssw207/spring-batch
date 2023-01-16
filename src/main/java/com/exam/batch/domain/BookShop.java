package com.exam.batch.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class BookShop {

    @Id
    private Long id;
}
