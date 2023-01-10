package com.exam.batch.job;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

// 스프링 배치 활성화를 위힌 설정
@Configuration
@EnableAutoConfiguration
@EnableBatchProcessing
public class TestBatchConfig {
}
