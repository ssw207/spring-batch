package com.exam.batch.job;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
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

	public static final int CHUNK_SIZE = 1000;
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory emf;
	private Long lastId = 0L;

	@Bean
	public Job myDbMigJob() {
		return jobBuilderFactory.get("migJob")
			.start(migStep())
			.incrementer(new RunIdIncrementer())
			.build();
	}

	@Bean
	public Step migStep() {
		return stepBuilderFactory.get("migStep")
			.<Product, Product2>chunk(CHUNK_SIZE)
			.reader(migReader())
			.writer(migWriter())
			.build();
	}

	@Bean
	public ItemReader<Product> migReader() {

		Map<String, Object> param = new HashMap<>();
		param.put("id", lastId);
		JpaPagingItemReader<Product> reader = new JpaPagingItemReader<>() {
			@Override
			public int getPage() { // 페이징 사이즈를 0으로 고정한다
				return 0;
			}

			@Override
			protected void doReadPage() {
				super.doReadPage();

				if (super.results != null) {
					lastId = results.get(results.size() - 1).getId();
				}
				log.info("lastId : {}", lastId);
			}
		};

		reader.setQueryString("SELECT p FROM Product p WHERE p.id > :id ORDER BY p.id");
		reader.setPageSize(CHUNK_SIZE);
		reader.setName("migReader");
		reader.setParameterValues(param);
		reader.setEntityManagerFactory(emf);
		return reader;
	}

	@Bean
	public ItemWriter<Product2> migWriter() {
		return new JpaItemWriterBuilder<Product2>()
			.usePersist(true)
			.entityManagerFactory(emf)
			.build();
	}
}
