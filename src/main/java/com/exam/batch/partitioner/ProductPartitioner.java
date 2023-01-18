package com.exam.batch.partitioner;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import javax.sql.DataSource;
import java.util.Map;

@RequiredArgsConstructor
public class ProductPartitioner implements Partitioner {

    private final DataSource dataSource;


    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        return null;
    }
}
