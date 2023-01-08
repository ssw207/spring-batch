package com.exam.batch.service;

import java.util.List;

import com.exam.batch.domain.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProductBatchService {


	private final List<Product> list;

	public Product read() {
		log.info("read call, list size {}",list.size());
		if (list.isEmpty()) {
			return null; // null이면 종료됨
		}

		Product product = list.get(0);
		list.remove(0);

		return product;
	}
}
