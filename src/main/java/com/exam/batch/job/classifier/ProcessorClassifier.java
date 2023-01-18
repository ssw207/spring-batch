package com.exam.batch.job.classifier;

import com.exam.batch.domain.ApiRequestVO;
import com.exam.batch.domain.ProductVO;
import lombok.Setter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.Classifier;

import java.util.HashMap;
import java.util.Map;

@Setter
public class ProcessorClassifier implements Classifier<ProductVO, ItemProcessor<?, ? extends ApiRequestVO>> {

    private Map<String, ItemProcessor<ProductVO, ApiRequestVO>> processorMap = new HashMap<>();

    @Override
    public ItemProcessor classify(ProductVO classifiable) { // ProductVO가 들어옴

        // type이 1,2,3이면 그에 맞는 아이펜 프로세서를 반화한다
        return processorMap.get(classifiable.getType());
    }
}
