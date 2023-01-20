package com.exam.batch.service;

import com.exam.batch.domain.ApiInfo;
import com.exam.batch.domain.ApiResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ApiService1 extends AbstractApiService {

    @Override
    protected ApiResponseVO doApiService(RestTemplate restTemplate, ApiInfo apiInfo) {
        log.info("서비스 1");

        ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:8081/api/product/1", apiInfo, String.class);

        return ApiResponseVO.builder().status(responseEntity.getStatusCodeValue())
                .msg(responseEntity.getBody())
                .build();
    }
}
