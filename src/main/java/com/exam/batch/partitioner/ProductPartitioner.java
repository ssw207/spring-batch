package com.exam.batch.partitioner;

import com.exam.batch.domain.ProductVO;
import com.exam.batch.job.api.QueryGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

// DB에서 전체 type을 읽어오고 type 개수만큰 stepExecution을 생성해 파티션에 할당한다
@RequiredArgsConstructor
public class ProductPartitioner implements Partitioner {

    private final DataSource dataSource;

    // 3개의 excution context를 사용한다. 각 context는 각 쓰레드가 사용한다
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        // 제품유형의 수만큼 나눈다
        ProductVO[] arr = QueryGenerator.getProductList(dataSource);
        Map<String, ExecutionContext> result = new HashMap<>();

        // ExecutionContext를 초기화
        for (int i = 0; i < arr.length; i++) {
            ExecutionContext value = new ExecutionContext(); // stepExcution을 생성 step에서 이 값에 접근할수 있다.
            value.put("product", arr[i]);
            result.put("partition" + i, value);
        }

        return result;
    }
}
