package com.exam.batch.listener;

import com.exam.batch.domain.Customer;
import com.exam.batch.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;

@Slf4j
public class CustomItemProcessorListener implements ItemProcessListener<Product, Customer> {

    @Override
    public void beforeProcess(Product item) {

    }

    @Override
    public void afterProcess(Product item, Customer result) {
        log.info("Thread process : {} {}", Thread.currentThread().getName(), item.getId());
    }

    @Override
    public void onProcessError(Product item, Exception e) {

    }
}
