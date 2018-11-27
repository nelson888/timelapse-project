package com.app4.project.timelapseserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEurekaClient
public class TimeLapseServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeLapseServerApplication.class, args);
	}

}
