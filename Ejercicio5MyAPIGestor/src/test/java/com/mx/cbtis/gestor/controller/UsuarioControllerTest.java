package com.mx.cbtis.gestor.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class UsuarioControllerTest extends BaseApiTest {

    @Autowired
    private UsuarioRepository repo;

    private Usuario nuevo(String nombre, String correo, String pass) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo_electronico(correo);
        u.setContrasena(pass);
        u.setRol("alumno");
        u.setImagen_usuario("foto.png");
        return u;
    }

    private Usuario guardar(String nombre, String correo, String pass) {
        Usuario guardado = repo.save(nuevo(nombre, correo, pass));
        sincronizar();
        return guardado;
    }

    @Test
    void getUsuarioLogin_credencialesCorrectas_devuelveElUsuario() throws Exception {
        guardar("Ana", "ana@cbtis.mx", "clave-ana");

        Usuario intento = new Usuario();
        intento.setCorreo_electronico("ana@cbtis.mx");
        intento.setContrasena("clave-ana");

        postJson("/getUsuarioLogin", intento)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void getUsuarioLogin_contrasenaIncorrecta_devuelveNombre000() throws Exception {
        guardar("Ana", "ana@cbtis.mx", "clave-ana");

        Usuario intento = new Usuario();
        intento.setCorreo_electronico("ana@cbtis.mx");
        intento.setContrasena("otra-clave");

        postJson("/getUsuarioLogin", intento)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("000"));
    }

    @Test
    void getUsuarioLogin_correoInexistente_devuelveNombre000() throws Exception {
        Usuario intento = new Usuario();
        intento.setCorreo_electronico("nadie@cbtis.mx");
        intento.setContrasena("x");

        postJson("/getUsuarioLogin", intento)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("000"));
    }

    @Test
    void buscarUsuario_existente_devuelveElRegistro() throws Exception {
        Usuario guardado = guardar("Luis", "luis@cbtis.mx", "clave-luis");

        mvc.perform(get("/buscarUsuario").param("id", String.valueOf(guardado.getId_usuario())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("luis@cbtis.mx")));
    }

    @Test
    void buscarUsuario_inexistente_noDevuelveElRegistroExistente() throws Exception {
        Usuario guardado = guardar("Luis", "luis@cbtis.mx", "clave-luis");
        int idInexistente = guardado.getId_usuario() + 1000;

        String resultado = cuerpoOExcepcion(get("/buscarUsuario").param("id", String.valueOf(idInexistente)));

        assertNoContiene(resultado, "luis@cbtis.mx");
    }

    @Test
    void addUsuario_valido_persiste() throws Exception {
        postJson("/addUsuario", nuevo("Eva", "eva@cbtis.mx", "clave-eva")).andExpect(status().isOk());

        assertEquals(1, repo.count());
    }

    @Test
    void addUsuario_sinCuerpo_devuelve400() throws Exception {
        mvc.perform(post("/addUsuario").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertEquals(0, repo.count());
    }

    @Test
    void updateUsuario_existente_actualizaElNombre() throws Exception {
        Usuario guardado = guardar("Luis", "luis@cbtis.mx", "clave-luis");
        guardado.setNombre("Luis Miguel");

        postJson("/updateUsuario", guardado).andExpect(status().isOk());
        sincronizar();

        assertEquals("Luis Miguel", repo.findById(guardado.getId_usuario()).orElseThrow().getNombre());
    }

    @Test
    void deleteUsuario_existente_loElimina() throws Exception {
        Usuario guardado = guardar("Luis", "luis@cbtis.mx", "clave-luis");

        deleteJson("/deleteUsuario", guardado).andExpect(status().isOk());
        sincronizar();

        assertTrue(repo.findById(guardado.getId_usuario()).isEmpty());
    }

    @Test
    void buscarUsuarios_conDatos_devuelveTodos() throws Exception {
        guardar("Ana", "ana@cbtis.mx", "a");
        guardar("Luis", "luis@cbtis.mx", "b");

        mvc.perform(post("/buscarUsuarios"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ana@cbtis.mx")))
                .andExpect(content().string(containsString("luis@cbtis.mx")));
    }

    @Test
    void buscarUsuarios_sinDatos_devuelveListaVacia() throws Exception {
        mvc.perform(post("/buscarUsuarios"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}
