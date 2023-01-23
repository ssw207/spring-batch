package com.exam.batch.job;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
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
	public Job myDbMigJob() {
		return jobBuilderFactory.get("migJob")
			.start(migStep())
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	@JobScope
	public Step migStep() {
		return stepBuilderFactory.get("migStep")
			.<Product, Product2>chunk(1000)
			.reader(migReader())
			.writer(migWriter())
			.build();
	}
	@Bean
	@StepScope
	public ItemReader<Product> migReader() {
		Map<String, Object> param = new HashMap<>();
		param.put("id", 0L);

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
