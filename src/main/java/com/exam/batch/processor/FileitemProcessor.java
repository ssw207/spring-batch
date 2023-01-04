package com.exam.batch.processor;

import com.exam.batch.domain.ProductVO;
import com.exam.batch.domain.Product;
import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

public class FileitemProcessor implements ItemProcessor<ProductVO, Product> {

    @Override
    public Product process(ProductVO item) throws Exception {

        ModelMapper modelMapper = new ModelMapper();
        Product product = modelMapper.map(item, Product.class);

        return product;
    }
}
