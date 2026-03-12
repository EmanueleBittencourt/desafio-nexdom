package com.nexdom.estoque_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.nexdom")
@EntityScan("com.nexdom.estoque.model")
public class EstoqueApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EstoqueApiApplication.class, args);
	}

}
