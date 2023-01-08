package com.exam.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.exam.batch.domain.Product;
import com.exam.batch.repository.ProductRepository;
import com.exam.batch.service.ProductBatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class IteamReaderAdaptorConfig {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final ProductRepository productRepository;

	@Bean
	public Job adaptorJob() throws Exception {
		return jobBuilderFactory.get("adaptorJob")
			.start(adaptorStep())
			.build();
	}

	@Bean
	public Step adaptorStep() throws Exception {
		return stepBuilderFactory.get("adaptorStep")
			.<Product, Product>chunk(10)
			.reader(adaptorReader())
			.writer(adaptorWriter())
			.build();
	}

	@Bean
	public ItemReader<Product> adaptorReader() throws Exception {
		log.info("adaptorReader call");
		ItemReaderAdapter<Product> reader = new ItemReaderAdapter<>(); // 리플랙션 기반으로 메서드호출
		ProductBatchService target = new ProductBatchService(productRepository.findAll()); // 어플리케이션 실행시 1회만 호출됨, 실제 이렇게 사용하면 안됨
		reader.setTargetObject(target); // 단건 read에 사용할 클래스
		reader.setTargetMethod("read"); // read메서드는 db에서 조회하는 작업이 아닌 chunk 객체에 입력하기위해 한건씩 데이터를 읽는 작업을한다
		return reader;
	}

	@Bean
	public ItemWriter<Object> adaptorWriter() {
		return items -> {
			items.forEach(System.out::println);
		};
	}
}
