package com.mx.cbtis.gestor.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ComentariosControllerTest extends BaseApiTest {

    @Autowired
    private ComentariosRepository repo;

    private Comentarios nuevo(String usuario, int tarea, String texto) {
        Comentarios c = new Comentarios();
        c.setId_usuario(usuario);
        c.setId_tarea(tarea);
        c.setTexto_comentario(texto);
        c.setFecha_hora("2026-10-28 10:00");
        return c;
    }

    private Comentarios guardar(String usuario, int tarea, String texto) {
        Comentarios guardado = repo.save(nuevo(usuario, tarea, texto));
        sincronizar();
        return guardado;
    }

    @Test
    void getComentario_porTarea_devuelveSoloLosDeEsaTarea() throws Exception {
        guardar("1", 5, "comentario-tarea-5-a");
        guardar("2", 5, "comentario-tarea-5-b");
        guardar("1", 6, "comentario-tarea-6");

        Comentarios filtro = new Comentarios();
        filtro.setId_tarea(5);

        postJson("/getComentario", filtro)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("comentario-tarea-5-a")))
                .andExpect(content().string(containsString("comentario-tarea-5-b")));

        String resultado = postJson("/getComentario", filtro).andReturn().getResponse().getContentAsString();
        assertNoContiene(resultado, "comentario-tarea-6");
    }

    @Test
    void getComentario_tareaSinComentarios_devuelveListaVacia() throws Exception {
        guardar("1", 5, "comentario-tarea-5-a");

        Comentarios filtro = new Comentarios();
        filtro.setId_tarea(999);

        postJson("/getComentario", filtro)
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void buscarComentario_existente_devuelveElRegistro() throws Exception {
        Comentarios guardado = guardar("1", 5, "texto-buscado");

        mvc.perform(get("/buscarComentario").param("id", String.valueOf(guardado.getId_comentario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("texto-buscado")));
    }

    @Test
    void buscarComentario_inexistente_noDevuelveElRegistroExistente() throws Exception {
        Comentarios guardado = guardar("1", 5, "texto-buscado");
        int idInexistente = guardado.getId_comentario() + 1000;

        String resultado = cuerpoOExcepcion(get("/buscarComentario").param("id", String.valueOf(idInexistente)));

        assertNoContiene(resultado, "texto-buscado");
    }

    @Test
    void addComentario_valido_persiste() throws Exception {
        postJson("/addComentario", nuevo("1", 5, "nuevo-comentario")).andExpect(status().isOk());

        assertEquals(1, repo.count());
    }

    @Test
    void addComentario_sinCuerpo_devuelve400() throws Exception {
        mvc.perform(post("/addComentario").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertEquals(0, repo.count());
    }

    @Test
    void updateComentario_existente_actualizaElTexto() throws Exception {
        Comentarios guardado = guardar("1", 5, "texto-original");
        guardado.setTexto_comentario("texto-modificado");

        postJson("/updateComentario", guardado).andExpect(status().isOk());
        sincronizar();

        assertEquals("texto-modificado",
                repo.findById(guardado.getId_comentario()).orElseThrow().getTexto_comentario());
    }

    @Test
    void deleteComentario_existente_loElimina() throws Exception {
        Comentarios guardado = guardar("1", 5, "texto-original");

        deleteJson("/deleteComentario", guardado).andExpect(status().isOk());
        sincronizar();

        assertTrue(repo.findById(guardado.getId_comentario()).isEmpty());
    }

    @Test
    void buscarComentarios_conDatos_devuelveTodos() throws Exception {
        guardar("1", 5, "primero");
        guardar("2", 6, "segundo");

        mvc.perform(post("/buscarComentarios"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("primero")))
                .andExpect(content().string(containsString("segundo")));
    }

    @Test
    void buscarComentarios_sinDatos_devuelveListaVacia() throws Exception {
        mvc.perform(post("/buscarComentarios"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}
