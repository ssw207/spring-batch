package com.exam.batch.job.test;

import com.exam.batch.entity.sales.SalesSum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BatchNoSpringContextUnitTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private ConfigurableApplicationContext context;
    private LocalDate orderDate;
    private BatchOnlyJdbcReaderTestConfiguration job;

    @BeforeEach
    public void setUp() {
        context = new AnnotationConfigApplicationContext(TestDataSourceConfiguration.class);
        dataSource = context.getBean("dataSource", DataSource.class); // 테스트에서 사용할 DB정보
        jdbcTemplate = new JdbcTemplate(dataSource); // DB정보를 이용해 JDBC템플릿 생성
        orderDate = LocalDate.of(2022, 4, 28);

        // job 실행시 필요한 정보 초기화
        job = new BatchOnlyJdbcReaderTestConfiguration(dataSource); // job실행정보 초기화
        job.setChunkSize(10); //JdbcPagingItemReaderBuilder 는 페이지 사이즈가 0 이면 에러발생함
    }

    // TODO 이건왜하는거지?
    @AfterEach
    public void tearDown() {
        // 생성한 bean을 destory
        if (this.context != null) {
            this.context.close();
        }
    }
    
    @Test
    public void 기간내_sales가_집계되어_salseSum이된다() throws Exception {
        //given
        long amount1 = 1000;
        long amount2 = 100;
        long amount3 = 10;
        jdbcTemplate.update("insert into sales (order_date, amount, order_no) values (?, ?, ?)", orderDate, amount1, "1");
        jdbcTemplate.update("insert into sales (order_date, amount, order_no) values (?, ?, ?)", orderDate, amount2, "2");
        jdbcTemplate.update("insert into sales (order_date, amount, order_no) values (?, ?, ?)", orderDate, amount3, "3");

        JdbcPagingItemReader<SalesSum> reader = job.batchOnlyJdbcReaderTestJobReader(orderDate.format(BatchOnlyJdbcReaderTestConfiguration.FORMATTER));
        reader.afterPropertiesSet(); // reader의 쿼리를 생성함. 이 메소드를 실행하지 않으면 쿼리가 null임

        //when, then
        assertThat(reader.read().getAmountSum()).isEqualTo(amount1 + amount2 + amount3); // read() 호출시 쿼리가 실행됨
        assertThat(reader.read()).isNull(); //조회 결과가 1이어야 하므로 그다음 쿼리 실행시 null 리턴
    }

}