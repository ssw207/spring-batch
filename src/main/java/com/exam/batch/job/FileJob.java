package com.exam.batch.job;

import com.exam.batch.domain.ProduceVO;
import com.exam.batch.domain.Product;
import com.exam.batch.processor.FileitemProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@Configuration
@RequiredArgsConstructor
public class FileJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("fileJob")
                .start(fileStep())
                .build();
    }

    @Bean
    @StepScope
    public Step fileStep() {
        return stepBuilderFactory.get("fileStep")
                .<ProduceVO, Product>chunk(10)
                .reader(fileItemReader(null))
                .processor(fileItemProcessor())
                .writer(fileWriter())
                .build();
    }

    @Bean
    public ItemWriter<Object> fileWriter() {
        return new JpaItemWriterBuilder<>()
                .entityManagerFactory(emf)
                .usePersist(true)
                .build();
    }

    @Bean
    public FlatFileItemReader<ProduceVO> fileItemReader(@Value("#{jobParameters['reqYmd']}") String reqYmd) {
        return new FlatFileItemReaderBuilder<ProduceVO>()
                .name("flatFile")
                .resource(new ClassPathResource("produce_"+ reqYmd +".csv"))
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
                .targetType(ProduceVO.class)
                .linesToSkip(1)
                .delimited().delimiter(",")
                .names("id", "name", "price")
                .build();
    }

    @Bean
    public ItemProcessor<ProduceVO, Product> fileItemProcessor() {
        return new FileitemProcessor();
    }
}



