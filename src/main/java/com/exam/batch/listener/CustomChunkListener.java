package com.exam.batch.listener;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomChunkListener implements ChunkListener {

	@Override
	public void beforeChunk(ChunkContext context) {
		log.info("beforeChunk");
	}

	@Override
	public void afterChunk(ChunkContext context) {
		log.info("afterChunk");
		context.getStepContext().getStepExecutionContext().entrySet().forEach(e -> {
			log.info("key: {}, value: {}", e.getKey(), e.getValue());
		});
	}

	@Override
	public void afterChunkError(ChunkContext context) {

	}
}
