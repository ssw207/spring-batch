package com.exam.batch.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.Getter;

@Data
@Entity
public class Book {

    @Id
    private Long id;
}