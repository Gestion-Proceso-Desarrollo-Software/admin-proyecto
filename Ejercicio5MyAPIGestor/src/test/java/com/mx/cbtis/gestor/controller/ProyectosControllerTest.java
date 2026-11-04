package com.mx.cbtis.gestor.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProtectosRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ProyectosControllerTest extends BaseApiTest {

    // OJO: asi se llama tu repositorio (con 'c'): ProtectosRepository
    @Autowired
    private ProtectosRepository repo;

    private Proyectos nuevo(String nombre) {
        Proyectos p = new Proyectos();
        p.setNombre(nombre);
        p.setDescripcion("Descripcion de " + nombre);
        p.setFecha_inicio("2026-10-01");
        p.setFecha_finalizacion("2026-12-01");
        p.setEstado_proyecto("Activo");
        p.setFondo("azul");
        return p;
    }

    private Proyectos guardar(String nombre) {
        Proyectos guardado = repo.save(nuevo(nombre));
        sincronizar();
        return guardado;
    }

    @Test
    void addProyecto_valido_persiste() throws Exception {
        postJson("/addProyecto", nuevo("Proyecto Alfa")).andExpect(status().isOk());

        assertEquals(1, repo.count());
    }

    @Test
    void addProyecto_sinCuerpo_devuelve400() throws Exception {
        mvc.perform(post("/addProyecto").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertEquals(0, repo.count());
    }

    @Test
    void buscarProyecto_existente_devuelveElRegistro() throws Exception {
        Proyectos guardado = guardar("Proyecto Alfa");

        mvc.perform(get("/buscarProyecto").param("id_proyecto", String.valueOf(guardado.getId_proyecto())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Proyecto Alfa")));
    }

    @Test
    void buscarProyecto_inexistente_noDevuelveElRegistroExistente() throws Exception {
        Proyectos guardado = guardar("Proyecto Alfa");
        int idInexistente = guardado.getId_proyecto() + 1000;

        String resultado = cuerpoOExcepcion(
                get("/buscarProyecto").param("id_proyecto", String.valueOf(idInexistente)));

        assertNoContiene(resultado, "Proyecto Alfa");
    }

    @Test
    void updateProyecto_existente_actualizaElNombre() throws Exception {
        Proyectos guardado = guardar("Proyecto Alfa");
        guardado.setNombre("Proyecto Beta");

        postJson("/updateProyecto", guardado).andExpect(status().isOk());
        sincronizar();

        assertEquals("Proyecto Beta", repo.findById(guardado.getId_proyecto()).orElseThrow().getNombre());
    }

    @Test
    void deleteProyecto_existente_loElimina() throws Exception {
        Proyectos guardado = guardar("Proyecto Alfa");

        deleteJson("/deleteProyecto", guardado).andExpect(status().isOk());
        sincronizar();

        assertTrue(repo.findById(guardado.getId_proyecto()).isEmpty());
    }

    @Test
    void buscarProyectos_conDatos_devuelveTodos() throws Exception {
        guardar("Proyecto Alfa");
        guardar("Proyecto Beta");

        mvc.perform(post("/buscarProyectos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Proyecto Alfa")))
                .andExpect(content().string(containsString("Proyecto Beta")));
    }

    @Test
    void buscarProyectos_sinDatos_devuelveListaVacia() throws Exception {
        mvc.perform(post("/buscarProyectos"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}
