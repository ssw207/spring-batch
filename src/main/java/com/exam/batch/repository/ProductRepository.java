package com.exam.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exam.batch.domain.Product;

public interface ProductRepository extends JpaRepository<Product, String> {

}
