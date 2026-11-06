package com.mx.cbtis;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

/** Verifica que el contexto arranca y que el RestTemplate se inyecta desde el @Bean. */
@SpringBootTest
class Ejercicio5ClienteApplicationTests {

    @Autowired
    private ApplicationContext contexto;

    @Test
    void contextoCarga_yExisteElRestTemplate() {
        assertNotNull(contexto.getBean(RestTemplate.class));
    }
}
