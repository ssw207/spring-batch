package com.exam.batch.job;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.exam.batch.reader.CustomChunkStreamReader;
import com.exam.batch.writer.CustomChunkStreamWirter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CustomChunkJobConfig {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job customChunkJob() {
		return jobBuilderFactory.get("customChunkJob")
			.start(customChunkStep())
			.build();
	}

	@Bean
	public Step customChunkStep() {
		return stepBuilderFactory.get("customStep")
			.<String, String>chunk(2)
			.reader(getCustomItemReader())
			.writer(getCustomItemWriter())
			.build();
	}

	@Bean
	public ItemReader<String> getCustomItemReader() {

		List<String> collect = IntStream.range(0, 10)
			.mapToObj(String::valueOf)
			.collect(Collectors.toList());

		return new CustomChunkStreamReader(collect);
	}

	@Bean
	public ItemWriter<String> getCustomItemWriter() {
		return new CustomChunkStreamWirter();
	}
}
