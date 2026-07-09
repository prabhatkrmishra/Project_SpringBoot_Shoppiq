package com.pkmprojects.shoppiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ShoppiqApplication {

	static void main(String[] args) {
		SpringApplication.run(ShoppiqApplication.class, args);
	}

}
