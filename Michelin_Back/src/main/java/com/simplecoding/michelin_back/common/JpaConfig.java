package com.simplecoding.michelin_back.common;

import org.springframework.context.annotation.Configuration;

/**
 * JPA 설정
 * Spring Boot 자동 설정(@EnableJpaAuditing은 MichelinBackApplication에서 처리)으로 충분하므로
 * 별도 @EnableJpaRepositories 선언 불필요
 */
@Configuration
public class JpaConfig {
}
