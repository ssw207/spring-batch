package com.exam.batch.job.file;

import com.exam.batch.domain.Product;
import com.exam.batch.domain.ProductVO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
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
public class FileJobConfig {

    private static final String PREFIX = "file";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    @Bean(PREFIX + "Job")
    public Job myFileJob() {
        return jobBuilderFactory.get(PREFIX + "Job")
                .start(step())
                .build();
    }

    @Bean(PREFIX + "Step")
    public Step step() {
        return stepBuilderFactory.get(PREFIX + "Step")
                .<ProductVO, Product>chunk(10)
                .reader(reader(null))
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean(PREFIX + "Reader")
    @StepScope
    public FlatFileItemReader<ProductVO> reader(@Value("#{jobParameters['reqYmd']}") String reqYmd) {
        return new FlatFileItemReaderBuilder<ProductVO>()
                .name("flatFile")
                .resource(new ClassPathResource("product_" + reqYmd + ".csv"))
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
                .targetType(ProductVO.class)
                .linesToSkip(1)
                .delimited().delimiter(",")
                .names("id", "name", "price")
                .build();
    }

    @Bean(PREFIX + "Processor")
    public ItemProcessor<ProductVO, Product> processor() {
        return item -> {

            ModelMapper modelMapper = new ModelMapper();
            Product product = modelMapper.map(item, Product.class);

            return product;
        };
    }

    @Bean(PREFIX + "Writer")
    public ItemWriter<Object> writer() {
        return new JpaItemWriterBuilder<>()
                .entityManagerFactory(emf)
                .usePersist(true)
                .build();
    }
}



