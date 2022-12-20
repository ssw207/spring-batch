package com.exam.batch.job.prac;

import com.exam.batch.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by seungwoo.song on 2022-08-17
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class MyJobConfiguration {

	public static final String JOB_NAME = "myPagingJob";

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource;
	private final int chunkSize = 10;

	@Bean
	public Job myPagingJob() throws Exception {
		return jobBuilderFactory.get(JOB_NAME)
			.start(pagingStep())
			.build();
	}

	private Step pagingStep() throws Exception {
		return stepBuilderFactory.get("myPagingStep")
			.<Member, Member>chunk(chunkSize)// <인풋 타입, 아웃풋타입>
			.reader(pagingReader())
			.processor(pagingProcessor())
			.writer(writer())
			.build();
	}

	private ItemWriter<? super Member> writer() {
		return null;
	}

	private ItemProcessor<? super Member,? extends Member> pagingProcessor() {
		return null;
	}

	private ItemReader<? extends Member> pagingReader() throws Exception {
		Map<String, Object> param = new HashMap<>();

		return new JdbcPagingItemReaderBuilder<Member>()
			.pageSize(chunkSize)
			.fetchSize(chunkSize) // 1회 조회건수
			.dataSource(dataSource)
			.rowMapper(new BeanPropertyRowMapper<>(Member.class)) // 결과를 매핑
			.queryProvider(createQueryProvider())
			.parameterValues(param)
			.name("pagingItemReader")
			.build();
	}

	private PagingQueryProvider createQueryProvider() throws Exception {

		SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
		queryProvider.setDataSource(dataSource);
		queryProvider.setSelectClause("member_id, member_seq");
		queryProvider.setFromClause("from member");
		//queryProvider.setWhereClause("");

		//정렬
		Map<String, Order> sortKeys = new HashMap<>(1);
		sortKeys.put("member_seq", Order.ASCENDING);

		queryProvider.setSortKeys(sortKeys);

		return queryProvider.getObject();
	}

}
