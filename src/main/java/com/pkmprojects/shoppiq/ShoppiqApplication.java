package com.pkmprojects.shoppiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import com.pkmprojects.shoppiq.config.PaginationProperties;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(PaginationProperties.class)
public class ShoppiqApplication {

	static void main(String[] args) {
		SpringApplication.run(ShoppiqApplication.class, args);
	}

}
