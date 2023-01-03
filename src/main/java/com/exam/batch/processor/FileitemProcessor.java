package com.exam.batch.processor;

import com.exam.batch.domain.ProduceVO;
import com.exam.batch.domain.Product;
import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

public class FileitemProcessor implements ItemProcessor<ProduceVO, Product> {

    @Override
    public Product process(ProduceVO item) throws Exception {

        ModelMapper modelMapper = new ModelMapper();
        Product product = modelMapper.map(item, Product.class);

        return product;
    }
}
