package com.exam.batch.partitioner;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import lombok.Setter;

@Setter
public class ColumnRangePartitioner implements Partitioner {
    private EntityManager entityManager;
    private String entityName;
    private String column;

    /**
     * Partition a database table assuming that the data in the column specified are
     * uniformly distributed. The execution context values will have keys
     * <code>minValue</code> and <code>maxValue</code> specifying the range of values to
     * consider in each partition.
     *
     * @see Partitioner#partition(int)
     */
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int min = entityManager.createQuery("SELECT MIN(t." + column + ") FROM " + entityName + " t ", Long.class).getSingleResult().intValue();
        int max = entityManager.createQuery("SELECT MAX(t." + column + ") FROM " + entityName + " t ", Long.class).getSingleResult().intValue();

        int targetSize = max - min / gridSize + 1;

        Map<String, ExecutionContext> result = new HashMap<>();
        int number = 0;
        int start = min;
        int end = start + targetSize - 1;

        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }
            value.putInt("min", start);
            value.putInt("max", end);
            start += targetSize;
            end += targetSize;
            number++;
        }

        return result;
    }
}
