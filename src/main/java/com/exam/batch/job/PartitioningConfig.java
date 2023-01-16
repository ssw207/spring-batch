package com.exam.batch.job;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.exam.batch.domain.Book;
import com.exam.batch.domain.BookShop;
import com.exam.batch.listener.StopWatchJobListener;
import com.exam.batch.partitioner.ColumnRangePartitioner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PartitioningConfig {
	private static final String PREFIX = "partitioning";
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;

	@Bean(name = PREFIX + "Job")
	public Job job() throws Exception {
		return jobBuilderFactory.get(PREFIX + "Job")
			.incrementer(new RunIdIncrementer())
			.start(masterStep())
			.listener(new StopWatchJobListener())// job 리스너 총실행시간을 측정한다
			.build();
	}

	@Bean(name = PREFIX + "MasterStep")
	public Step masterStep() throws Exception {
	    return stepBuilderFactory.get(PREFIX + "MasterStep")
	        .partitioner(slaveStep().getName(), partitioner())
	        .step(slaveStep())
	        .gridSize(4)
	        .taskExecutor(new SimpleAsyncTaskExecutor())
	        .build();
	}


	public Partitioner partitioner() {
		ColumnRangePartitioner partitioner = new ColumnRangePartitioner(dataSource, "book", "id");
		return partitioner;
	}

	@Bean(name = PREFIX + "SlaveStep")
	public Step slaveStep() throws Exception {
		return stepBuilderFactory.get("slaveStep")
			.<Book, BookShop>chunk(5)
			.reader(reader(null, null))
			.writer(writer())
			.build();
	}

	@StepScope
	@Bean(name = PREFIX + "Reader")
	public ItemReader<Book> reader(
		@Value("#{stepExecutionContext['min']}") Long minValue,
		@Value("#{stepExecutionContext['max']}") Long maxValue
	) throws Exception {
		SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
		queryProvider.setDataSource(dataSource);
		queryProvider.setSelectClause("id");
		queryProvider.setFromClause("from book");
		queryProvider.setWhereClause("where id >= " + minValue + " and id < " + maxValue);

		Map<String, Order> sortKeys = new HashMap<>();
		sortKeys.put("id", Order.ASCENDING);

		queryProvider.setSortKeys(sortKeys);

		return new JdbcPagingItemReaderBuilder<Book>()
			.name("jdbcPagingReader")
			.pageSize(10)
			.dataSource(dataSource)
			.rowMapper(new BeanPropertyRowMapper<>(Book.class))
			.queryProvider(queryProvider.getObject())
			.build();
	}

	@Bean(name = PREFIX + "Writer")
	public ItemWriter<BookShop> writer() {
		return new JdbcBatchItemWriterBuilder<BookShop>()
			.dataSource(dataSource)
			.sql("INSERT INTO BOOK_SHOP (ID) VALUES (:id)")
			.beanMapped() // Customer의 필드명과 SQL의 파라미터명이 같아야 한다.
			.build();
	}

	@Bean
	public TaskExecutor partitioningTaskExecutor() {
	    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
	    taskExecutor.setCorePoolSize(4); // 최초 풀 초기화시 4개 쓰레드생성
	    taskExecutor.setMaxPoolSize(8); // 큐에 요청이 가득차면 쓰레드를 최대 8개까지 생성
	    taskExecutor.setThreadNamePrefix("async-Thread");
	    return taskExecutor;
	}
}


