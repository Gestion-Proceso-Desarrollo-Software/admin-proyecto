package com.mx.cbtis.gestor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base de las pruebas de los 5 controladores del backend.
 * - @SpringBootTest + @AutoConfigureMockMvc: arranca el contexto real (controlador + repositorio).
 * - @ActiveProfiles("test"): usa H2 (application-test.properties) y no MariaDB.
 * - @Transactional: cada prueba hace rollback, asi que no se contaminan entre si.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseApiTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @PersistenceContext
    protected EntityManager em;

    /** Escribe a la BD y limpia la cache de Hibernate para que las lecturas vayan a la tabla. */
    protected void sincronizar() {
        em.flush();
        em.clear();
    }

    protected ResultActions postJson(String url, Object cuerpo) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuerpo)));
    }

    protected ResultActions deleteJson(String url, Object cuerpo) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.delete(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(cuerpo)));
    }

    /**
     * Para el caso "id inexistente": devuelve el cuerpo de la respuesta o, si el controlador
     * lanza una excepcion, el texto "EXCEPCION:<tipo>". Asi la prueba se puede fijar al
     * comportamiento real de TU controlador (ver capitulo 6 del manual).
     */
    protected String cuerpoOExcepcion(MockHttpServletRequestBuilder peticion) {
        try {
            return mvc.perform(peticion).andReturn().getResponse().getContentAsString();
        } catch (Exception ex) {
            Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
            return "EXCEPCION:" + causa.getClass().getSimpleName();
        }
    }

    protected static void assertNoContiene(String texto, String fragmento) {
        Assertions.assertFalse(texto.contains(fragmento),
                "No debia aparecer '" + fragmento + "' en: " + texto);
    }
}
