package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * RestClientConfig — RestClient 빈 등록
 *
 * Spring Boot 3.2+에서 RestTemplate 대신 권장되는 RestClient를
 * 싱글턴 빈으로 등록한다.
 * InventoryService에서 정보나루 bookExist API 호출 시 사용한다.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
