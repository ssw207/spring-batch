package com.exam.batch.service;

import com.exam.batch.domain.ApiInfo;
import com.exam.batch.domain.ApiResponseVO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService2 extends AbstractApiService {

    @Override
    protected ApiResponseVO doApiService(RestTemplate restTemplate, ApiInfo apiInfo) {

        ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:8082/api/product/1", apiInfo, String.class);

        return ApiResponseVO.builder().status(responseEntity.getStatusCodeValue())
                .msg(responseEntity.getBody())
                .build();
    }
}
