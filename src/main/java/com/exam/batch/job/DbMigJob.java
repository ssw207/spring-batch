package com.exam.batch.job;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.exam.batch.domain.Product;
import com.exam.batch.domain.Product2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DbMigJob {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory emf;

	@Bean
	public Job dbMigJob() {
		return jobBuilderFactory.get("migJob")
			.start(migStep())
			.build();
	}

	@Bean
	public Step migStep() {
		return stepBuilderFactory.get("migStep")
			.<Product, Product2>chunk(1000)
			.reader(migReader(null, null))
			.writer(migWriter())
			.build();
	}
	@Bean
	@StepScope
	public ItemReader<Product> migReader(
		@Value("#{jobParameters[id]}") String jobId,
		@Value("#{stepExecutionContext[id]}") String stepId
	) {
		String id = (stepId == null) ? jobId : stepId;

		Map<String, Object> param = new HashMap<>();
		param.put("id", id);

		return new JpaPagingItemReaderBuilder<Product>()
			.entityManagerFactory(emf)
			.pageSize(1000)
			.name("migReader")
			.queryString("SELECT p FROM Product p WHERE p.id > :id")
			.parameterValues(param)
			.build();
	}

	@Bean
	public ItemWriter<Product2> migWriter() {
		return new JpaItemWriterBuilder<Product2>()
			.usePersist(true)
			.entityManagerFactory(emf)
			.build();
	}

}
