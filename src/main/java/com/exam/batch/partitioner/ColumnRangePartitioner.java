package com.exam.batch.partitioner;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class ColumnRangePartitioner implements Partitioner {
    private JdbcTemplate jdbcTeamplate;
    private String tableName;
    private String column;

    public ColumnRangePartitioner(DataSource dataSource, String tableName, String column) {
        this.jdbcTeamplate = new JdbcTemplate(dataSource);
        this.tableName = tableName;
        this.column = column;
    }

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
        int min = jdbcTeamplate.queryForObject("SELECT MIN(t." + column + ") FROM " + tableName + " t ", Integer.class);
        int max = jdbcTeamplate.queryForObject("SELECT MAX(t." + column + ") FROM " + tableName + " t ",  Integer.class);

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
