package com.exam.batch.job.test;

import com.exam.batch.entity.sales.SalesSum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@EnableBatchProcessing
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        StepScopeTestExecutionListener.class
})
@ContextConfiguration(classes = {
        BatchJdbcTestConfiguration.class,
        BatchJdbcUnitTestJobConfigurationLegacyTest.TestDataSourceConfiguration.class
})
public class BatchJdbcUnitTestJobConfigurationLegacyTest {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private JdbcPagingItemReader<SalesSum> reader;

    @Autowired
    private DataSource dataSource;

    private JdbcOperations jdbcTemplate;
    private LocalDate orderDate = LocalDate.of(2019, 10, 6);

    public StepExecution getStepExecution() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("orderDate", this.orderDate.format(FORMATTER))
                .toJobParameters();

        return MetaDataInstanceFactory.createStepExecution(jobParameters);
    }

    @BeforeEach
    public void setUp() {
        reader.setDataSource(this.dataSource);
        jdbcTemplate = new JdbcTemplate(this.dataSource);
    }

    @AfterEach
    public void tearDown() {
        this.jdbcTemplate.update("delete from sales");
    }

    @Test
    public void ?????????_Sales???_????????????_SalesSum?????????() throws Exception {
        //given
        long amount1 = 1000;
        long amount2 = 500;
        long amount3 = 100;

        saveSales(amount1, "1");
        saveSales(amount2, "2");
        saveSales(amount3, "3");

        // when && then
        assertThat(reader.read().getAmountSum()).isEqualTo(amount1+amount2+amount3);
        assertThat(reader.read()).isNull();
    }

    private void saveSales(long amount, String orderNo) {
        jdbcTemplate.update("insert into sales (order_date, amount, order_no) values (?, ?, ?)", this.orderDate, amount, orderNo);
    }

    @Configuration
    public static class TestDataSourceConfiguration {
        private static final String CREATE_SQL =
                "create table IF NOT EXISTS sales (id bigint not null auto_increment, amount bigint not null, order_date date, order_no varchar(255), primary key (id));";

        @Bean
        public DataSource dataSource() {
            EmbeddedDatabaseFactory databaseFactory = new EmbeddedDatabaseFactory();
            databaseFactory.setDatabaseType(EmbeddedDatabaseType.H2);
            return databaseFactory.getDatabase();
        }

        @Bean
        public DataSourceInitializer initializer(DataSource dataSource) {
            DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
            dataSourceInitializer.setDataSource(dataSource);

            Resource create = new ByteArrayResource(CREATE_SQL.getBytes());
            dataSourceInitializer.setDatabasePopulator(new ResourceDatabasePopulator(create));

            return dataSourceInitializer;
        }
    }
}
