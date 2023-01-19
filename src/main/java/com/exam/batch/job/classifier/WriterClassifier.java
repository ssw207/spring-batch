package com.exam.batch.job.classifier;

import com.exam.batch.domain.ApiRequestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

import java.util.Map;

@RequiredArgsConstructor
public class WriterClassifier implements Classifier<ApiRequestVO, ItemWriter<? super ApiRequestVO>> {

    private final Map<String, ItemWriter<ApiRequestVO>> processorMap;

    @Override
    public ItemWriter<? super ApiRequestVO> classify(ApiRequestVO classifiable) {
        return processorMap.get(classifiable.getProductVO().getType());
    }
}
