package com.mx.cbtis.gestor.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.TareasRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class TareasControllerTest extends BaseApiTest {

    @Autowired
    private TareasRepository repo;

    private Tareas nueva(String nombre, int proyecto) {
        Tareas t = new Tareas();
        t.setNombre(nombre);
        t.setDescripcion("Descripcion de " + nombre);
        t.setFecha_vencimiento("2026-11-30");
        t.setLista("Por hacer");
        t.setEstado("Pendiente");
        t.setId_proyecto(proyecto);
        return t;
    }

    private Tareas guardar(String nombre, int proyecto) {
        Tareas guardada = repo.save(nueva(nombre, proyecto));
        sincronizar();
        return guardada;
    }

    @Test
    void getTarea_porProyecto_devuelveSoloLasDeEseProyecto() throws Exception {
        guardar("tarea-p1-a", 1);
        guardar("tarea-p1-b", 1);
        guardar("tarea-p2", 2);

        Tareas filtro = new Tareas();
        filtro.setId_proyecto(1);

        postJson("/getTarea", filtro)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tarea-p1-a")))
                .andExpect(content().string(containsString("tarea-p1-b")));

        String resultado = postJson("/getTarea", filtro).andReturn().getResponse().getContentAsString();
        assertNoContiene(resultado, "tarea-p2");
    }

    @Test
    void getTarea_proyectoSinTareas_devuelveListaVacia() throws Exception {
        guardar("tarea-p1-a", 1);

        Tareas filtro = new Tareas();
        filtro.setId_proyecto(999);

        postJson("/getTarea", filtro)
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void buscarTarea_existente_devuelveElRegistro() throws Exception {
        Tareas guardada = guardar("tarea-buscada", 1);

        mvc.perform(get("/buscarTarea").param("id_tarea", String.valueOf(guardada.getId_tarea())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tarea-buscada")));
    }

    @Test
    void buscarTarea_inexistente_noDevuelveElRegistroExistente() throws Exception {
        Tareas guardada = guardar("tarea-buscada", 1);
        int idInexistente = guardada.getId_tarea() + 1000;

        String resultado = cuerpoOExcepcion(get("/buscarTarea").param("id_tarea", String.valueOf(idInexistente)));

        assertNoContiene(resultado, "tarea-buscada");
    }

    @Test
    void addTareas_valida_persiste() throws Exception {
        postJson("/addTareas", nueva("tarea-nueva", 1)).andExpect(status().isOk());

        assertEquals(1, repo.count());
    }

    @Test
    void addTareas_sinCuerpo_devuelve400() throws Exception {
        mvc.perform(post("/addTareas").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertEquals(0, repo.count());
    }

    @Test
    void updateTareas_existente_actualizaElEstado() throws Exception {
        Tareas guardada = guardar("tarea-original", 1);
        guardada.setEstado("Terminada");

        postJson("/updateTareas", guardada).andExpect(status().isOk());
        sincronizar();

        assertEquals("Terminada", repo.findById(guardada.getId_tarea()).orElseThrow().getEstado());
    }

    @Test
    void deleteTareas_existente_laElimina() throws Exception {
        Tareas guardada = guardar("tarea-original", 1);

        deleteJson("/deleteTareas", guardada).andExpect(status().isOk());
        sincronizar();

        assertTrue(repo.findById(guardada.getId_tarea()).isEmpty());
    }

    @Test
    void buscarTareas_conDatos_devuelveTodas() throws Exception {
        guardar("tarea-uno", 1);
        guardar("tarea-dos", 2);

        mvc.perform(post("/buscarTareas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tarea-uno")))
                .andExpect(content().string(containsString("tarea-dos")));
    }

    @Test
    void buscarTareas_sinDatos_devuelveListaVacia() throws Exception {
        mvc.perform(post("/buscarTareas"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}
