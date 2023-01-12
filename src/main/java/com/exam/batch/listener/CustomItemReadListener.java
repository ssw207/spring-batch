package com.exam.batch.listener;

import com.exam.batch.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;

@Slf4j
public class CustomItemReadListener implements ItemReadListener<Product> {

    @Override
    public void beforeRead() {

    }

    @Override
    public void afterRead(Product item) {
        log.info("Thread read : {} {}", Thread.currentThread().getName(), item.getId());
    }

    @Override
    public void onReadError(Exception ex) {

    }
}
