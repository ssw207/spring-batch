package com.exam.batch.job.api;

import com.exam.batch.domain.ProductVO;
import com.exam.batch.rowmapper.ProductRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryGenerator {

    public static ProductVO[] getProductList(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        List<ProductVO> list = jdbcTemplate.query("select distinct type from product", new ProductRowMapper() {
            @Override
            public ProductVO mapRow(ResultSet rs, int rowNum) throws SQLException {
                return ProductVO.builder().type(rs.getString("type")).build();
            }
        });

        return list.toArray(new ProductVO[]{});
    }

    public static Map<String, Object> getParameterForQuery(String param, String value) { // 파티션을 값을 파라미터 이름으로 매핑함
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(param, value);
        return parameters;
    }
}
