package com.exam.batch.reader;

import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomChunkStreamReader implements ItemStreamReader<String> {

	private final List<String> items;
	private int index = -1;
	private boolean restart = false;

	public CustomChunkStreamReader(List<String> items) {
		this.items = items;
	}

	@Override
	public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if (index >= items.size()) {
			return null;
		}

		String item = items.get(index);
		index++;

		if (index == 6 && !restart) {
			log.info("Restarting the job");
			throw new RuntimeException("Restarting the job");
		}

		return item;
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		if (executionContext.containsKey("index")) { // step 실행컨텍스트에 index 값이 있으면
			index = executionContext.getInt("index"); // 값을세팅한다
			restart = false;
		} else {
			index = 0;
			executionContext.putInt("index", index);
		}
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		executionContext.putInt("index", index); // step 실행컨텍스트에 index 값을 저장한다
		log.info("read update 호출 - executionContext.getInt(\"index\") = {}", executionContext.getInt("index"));
	}

	@Override
	public void close() throws ItemStreamException {
		log.info("close호출");
	}
}
