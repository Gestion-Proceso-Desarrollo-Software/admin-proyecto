package com.mx.cbtis.gestor.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class AsignacionesControllerTest extends BaseApiTest {

    @Autowired
    private AsignacionesRepository repo;

    private Asignaciones nueva(int usuario, int tarea, String fecha) {
        Asignaciones a = new Asignaciones();
        a.setId_usuario(usuario);
        a.setId_tarea(tarea);
        a.setFecha_asignacion(fecha);
        return a;
    }

    private Asignaciones guardar(int usuario, int tarea, String fecha) {
        Asignaciones guardada = repo.save(nueva(usuario, tarea, fecha));
        sincronizar();
        return guardada;
    }

    @Test
    void addAsignacion_valida_devuelve1_yPersiste() throws Exception {
        postJson("/addAsignacion", nueva(1, 10, "2026-10-28"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        assertEquals(1, repo.count());
    }

    @Test
    void addAsignacion_sinCuerpo_devuelve400_yNoPersiste() throws Exception {
        mvc.perform(post("/addAsignacion").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertEquals(0, repo.count());
    }

    @Test
    void buscarAsignacion_existente_devuelveElRegistro() throws Exception {
        Asignaciones guardada = guardar(1, 10, "2026-10-28");

        mvc.perform(get("/buscarAsignacion").param("id", String.valueOf(guardada.getId_asignacion())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2026-10-28")));
    }

    @Test
    void buscarAsignacion_inexistente_noDevuelveElRegistroExistente() throws Exception {
        Asignaciones guardada = guardar(1, 10, "2026-10-28");
        int idInexistente = guardada.getId_asignacion() + 1000;

        String resultado = cuerpoOExcepcion(get("/buscarAsignacion").param("id", String.valueOf(idInexistente)));

        assertNoContiene(resultado, "2026-10-28");
    }

    @Test
    void updateAsignacion_existente_actualizaLaFecha() throws Exception {
        Asignaciones guardada = guardar(1, 10, "2026-10-28");
        guardada.setFecha_asignacion("2026-11-05");

        postJson("/updateAsignacion", guardada).andExpect(status().isOk());
        sincronizar();

        assertEquals("2026-11-05", repo.findById(guardada.getId_asignacion()).orElseThrow().getFecha_asignacion());
        assertEquals(1, repo.count());
    }

    @Test
    void deleteAsignacion_existente_loElimina() throws Exception {
        Asignaciones guardada = guardar(1, 10, "2026-10-28");

        deleteJson("/deleteAsignacion", guardada).andExpect(status().isOk());
        sincronizar();

        assertTrue(repo.findById(guardada.getId_asignacion()).isEmpty());
    }

    @Test
    void buscarAsignaciones_conDatos_devuelveTodas() throws Exception {
        guardar(1, 10, "2026-10-28");
        guardar(2, 11, "2026-10-29");

        mvc.perform(post("/buscarAsignaciones"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2026-10-28")))
                .andExpect(content().string(containsString("2026-10-29")));
    }

    @Test
    void buscarAsignaciones_sinDatos_devuelveListaVacia() throws Exception {
        mvc.perform(post("/buscarAsignaciones"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}
