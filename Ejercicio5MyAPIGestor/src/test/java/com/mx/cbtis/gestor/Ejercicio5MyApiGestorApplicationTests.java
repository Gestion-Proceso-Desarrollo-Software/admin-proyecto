package com.mx.cbtis.gestor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/** Con el perfil "test" usa H2 (application-test.properties); sin el, intentaria conectar a MariaDB. */
@SpringBootTest
@ActiveProfiles("test")
class Ejercicio5MyApiGestorApplicationTests {

	@Autowired
	private ApplicationContext contexto;

	@Test
	void contextLoads() {
		assertNotNull(contexto);
	}

}
