package com.exam.batch.listener;

import com.exam.batch.domain.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;

import java.util.List;

@Slf4j
public class CustomItemWiterListener implements ItemWriteListener<Customer> {

    @Override
    public void beforeWrite(List<? extends Customer> items) {

    }

    @Override
    public void afterWrite(List<? extends Customer> items) {
        log.info("Thread write : {} {}", Thread.currentThread().getName(), items.size());
    }

    @Override
    public void onWriteError(Exception exception, List<? extends Customer> items) {

    }
}
