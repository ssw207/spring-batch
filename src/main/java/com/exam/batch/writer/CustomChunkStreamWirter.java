package com.exam.batch.writer;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

import lombok.extern.slf4j.Slf4j;

/** 구현체 호출확인을 위한 로깅만 처리 */
@Slf4j
public class CustomChunkStreamWirter implements ItemStreamWriter<String> {

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		log.info("writer-open");
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		log.info("writer-update");
	}

	@Override
	public void close() throws ItemStreamException {
		log.info("writer-close");
	}

	@Override
	public void write(List<? extends String> items) throws Exception {
		log.info("writer-write");
		items.forEach(log::info);
	}
}
