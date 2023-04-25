package com.example.mashup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MashupApplication {

	public static void main(String[] args) {
		SpringApplication.run(MashupApplication.class, args);
	}

}
