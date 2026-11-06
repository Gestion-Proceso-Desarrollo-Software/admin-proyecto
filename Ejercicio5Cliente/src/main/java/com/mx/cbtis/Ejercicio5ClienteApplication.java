package com.mx.cbtis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Ejercicio5ClienteApplication {

	public static void main(String[] args) {
		SpringApplication.run(Ejercicio5ClienteApplication.class, args);
	}

	/** Un solo RestTemplate compartido; el controlador lo recibe por constructor. */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
