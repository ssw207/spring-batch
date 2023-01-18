package com.exam.batch.job.classifier;

import com.exam.batch.domain.ApiRequestVO;
import lombok.Setter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

import java.util.HashMap;
import java.util.Map;

@Setter
public class WriterClassifier implements Classifier<ApiRequestVO, ItemWriter<? super ApiRequestVO>> {

    private Map<String, ItemWriter<ApiRequestVO>> processorMap = new HashMap<>();

    @Override
    public ItemWriter<? super ApiRequestVO> classify(ApiRequestVO classifiable) {
        return processorMap.get(classifiable.getProductVO().getType());
    }
}
