package com.exam.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
	스프링 배치 기능 활성화.
	어플리케이션 Bean으로 등록된 Job을 검색후 초기화 + Job을 자동으로 실행

	1) SimpleBatchConfiguration
		JobBuilderFactory 와 StepBuilderFactory 생성
		스프링 배치의 주요 구성 요소 생성 - 프록시 객체로 생성됨

	2) BatchConfigurerConfiguration
		BasicBatchConfigurer
			SimpleBatchConfiguration 에서 생성한 프록시 객체의 실제 대상 객체를 생성하는 설정 클래스
			빈으로 의존성 주입 받아서 주요 객체들을 참조해서 사용할 수 있다
		JpaBatchConfigurer
			JPA 관련 객체를 생성하는 설정 클래스
			사용자 정의 BatchConfigurer 인터페이스를 구현하여 사용할 수 있음

	3) BatchAutoConfiguration
		스프링 배치가 초기화 될 때 자동으로 실행되는 설정 클래스
		Job 을 수행하는 JobLauncherApplicationRunner 빈을 생성 -> 어플리케이션 실행시 배치를 자동으로 실행하는 설정
 */
@EnableBatchProcessing
@SpringBootApplication
public class BatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}

}
