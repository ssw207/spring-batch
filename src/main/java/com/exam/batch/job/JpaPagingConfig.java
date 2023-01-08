package com.exam.batch.job;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManagerFactory;

import org.modelmapper.ModelMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.exam.batch.domain.Customer;
import com.exam.batch.domain.Product;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JpaPagingConfig {
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory emf;
	private final ModelMapper modelMapper = new ModelMapper();

	@Bean
	public Job jpaPagingJob() throws Exception {
		return jobBuilderFactory.get("jpaPagingJob")
			.start(jpaPagingStep())
			.incrementer(getIncrementer())
			.build();
	}

	private JobParametersIncrementer getIncrementer() {
		return parameters -> new JobParametersBuilder(Optional.ofNullable(parameters).orElse(new JobParameters()))
			.addLong("time", System.currentTimeMillis())
			.toJobParameters();
	}

	@Bean
	public Step jpaPagingStep() throws Exception {
		return stepBuilderFactory.get("jpaPagingStep")
			.<Product, Customer>chunk(10)
			.reader(jpaPagingReader())
			.processor(jpbProcessor())
			.writer(jpaPagingWriter())
			.build();
	}

	@Bean
	public ItemProcessor<? super Product, ? extends Customer> jpbProcessor() {
		return (ItemProcessor<Product, Customer>)item -> modelMapper.map(item, Customer.class);
	}

	@Bean
	public ItemReader<Product> jpaPagingReader() throws Exception {
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("price", 10);

		return new JpaPagingItemReaderBuilder<Product>()
			.name("jpaPagingReader")
			.pageSize(10)
			.entityManagerFactory(emf)
			.queryString("SELECT p FROM Product p WHERE p.price > :price")
			.parameterValues(parameterValues)
			.build();
	}

	@Bean
	public ItemWriter<Customer> jpaPagingWriter() {
		return new JpaItemWriterBuilder<Customer>()
			.usePersist(true)
			.entityManagerFactory(emf)
			.build();
	}
}
