package com.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.back.Controllers", "com.back.Configuration"})
public class BackApplication {

	public static void main(String[] args) {
		System.out.println("app to run");
		SpringApplication.run(BackApplication.class, args);
		System.out.println("app running");
	}
}
