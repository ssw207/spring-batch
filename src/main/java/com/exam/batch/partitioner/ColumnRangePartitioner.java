package com.exam.batch.partitioner;

import lombok.Setter;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

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
        String minStr = entityManager.createQuery("SELECT MIN(t." + column + ") FROM " + entityName + " t ", String.class).getSingleResult();
        String maxStr = entityManager.createQuery("SELECT MAX(t." + column + ") FROM " + entityName + " t ", String.class).getSingleResult();
        int min = Integer.parseInt(minStr);
        System.out.println("min = " + min);
        int max = Integer.parseInt(maxStr);
        System.out.println("max = " + max);
        int targetSize = (max - min) / gridSize + 1;

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
