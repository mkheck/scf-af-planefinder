package com.thehecklers.scfafplanefinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class ScfAfPlanefinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScfAfPlanefinderApplication.class, args);
	}

	@Bean
	WebClient.Builder builder() {
		return WebClient.builder();
	}
}
