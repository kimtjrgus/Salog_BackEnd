package com.codemouse.salog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // DB 조회 성능 향상을 위해 캐싱 적용
public class SalogApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalogApplication.class, args);
	}

}
