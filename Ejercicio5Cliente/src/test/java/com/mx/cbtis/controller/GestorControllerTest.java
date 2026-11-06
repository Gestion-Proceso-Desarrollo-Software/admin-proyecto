package com.mx.cbtis.controller;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;

import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Pruebas de GestorController SIN Spring ni backend real:
 *  - MockRestServiceServer intercepta las llamadas del RestTemplate a http://localhost:8081
 *    (funciona con postForObject, exchange, delete... porque actua a nivel HTTP).
 *  - MockMvc standalone prueba las rutas del controlador; InternalResourceViewResolver maneja
 *    tanto vistas normales como "redirect:/...".
 */
class GestorControllerTest {

    private static final String API = "http://localhost:8081";
    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    private static final String PROYECTO = "{\"id_proyecto\":1,\"nombre\":\"Proyecto A\",\"descripcion\":\"Demo\","
            + "\"fecha_inicio\":\"2026-10-01\",\"fecha_finalizacion\":\"2026-12-01\",\"estado_proyecto\":\"Activo\",\"fondo\":\"azul\"}";
    private static final String USUARIO = "{\"id_usuario\":1,\"nombre\":\"Ana\",\"correo_electronico\":\"ana@cbtis.mx\","
            + "\"contrasena\":\"1234\",\"rol\":\"alumno\",\"imagen_usuario\":\"ana.png\"}";
    private static final String USUARIO_NO_EXISTE = "{\"id_usuario\":null,\"nombre\":\"000\",\"correo_electronico\":null,"
            + "\"contrasena\":null,\"rol\":null,\"imagen_usuario\":null}";
    private static final String TAREA = "{\"id_tarea\":4,\"nombre\":\"Tarea A\",\"descripcion\":\"Demo\","
            + "\"fecha_vencimiento\":\"2026-11-30\",\"lista\":\"Por hacer\",\"estado\":\"Pendiente\",\"id_proyecto\":1}";
    private static final String COMENTARIO = "{\"id_comentario\":2,\"id_usuario\":\"1\",\"id_tarea\":4,"
            + "\"texto_comentario\":\"Comentario A\",\"fecha_hora\":\"2026-10-28 10:00\"}";

    private MockRestServiceServer backend;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        RestTemplate restTemplate = new RestTemplate();
        backend = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        mvc = MockMvcBuilders.standaloneSetup(new GestorController(restTemplate))
                .setViewResolvers(viewResolver)
                .build();
    }

    /** Todas las expectativas "obligatorias" deben haberse usado: asi probamos que SI se llamo al backend. */
    @AfterEach
    void verificar() {
        backend.verify();
    }

    // ---------- utilidades ----------

    private void backendResponde(HttpMethod metodo, String ruta, String json) {
        backend.expect(requestTo(API + ruta)).andExpect(method(metodo))
                .andRespond(withSuccess(json, JSON));
    }

    /** Para rutas que quiza llamen al backend o no (el inventario no lo aclara). */
    private void backendOpcional(HttpMethod metodo, String ruta, String json) {
        backend.expect(ExpectedCount.between(0, Integer.MAX_VALUE), requestTo(API + ruta))
                .andExpect(method(metodo)).andRespond(withSuccess(json, JSON));
    }

    // ---------- 1-4: paginas estaticas (home, tutorial, manual, login) ----------

    @ParameterizedTest(name = "GET {0} => vista {1}")
    @CsvSource({"/,home", "/tutorial,tutorial", "/manual,manual", "/login,login"})
    void paginasEstaticas_devuelvenSuVista(String ruta, String vista) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isOk()).andExpect(view().name(vista));
    }

    // ---------- 5: register ----------

    @Test
    void register_agregaUsuarioVacioAlModelo() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("usuario"));
    }

    // ---------- 6: saveUsuario ----------

    @Test
    void saveUsuario_ok_enviaAlBackend_yRedirigeALogin() throws Exception {
        backendResponde(HttpMethod.POST, "/addUsuario", "1");

        mvc.perform(post("/saveUsuario")
                        .param("nombre", "Ana").param("correo_electronico", "ana@cbtis.mx")
                        .param("contrasena", "1234").param("rol", "alumno"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login*"));
    }

    // ---------- 7: inicio ----------

    @Test
    void inicio_muestraLaVistaInicio_conProyectos() throws Exception {
        backendOpcional(HttpMethod.POST, "/buscarProyectos", "[" + PROYECTO + "]");

        mvc.perform(get("/inicio"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attributeExists("proyectos"));
    }

    // ---------- 8: saveProyecto ----------

    @Test
    void saveProyecto_ok_enviaAlBackend_yRedirigeAInicio() throws Exception {
        backendResponde(HttpMethod.POST, "/addProyecto", "1");

        mvc.perform(post("/saveProyecto").param("nombre", "Proyecto A").param("descripcion", "Demo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/inicio*"));
    }

    // ---------- 9: consultarProyectos ----------

    @Test
    void consultarProyectos_devuelveLaListaDelBackend() throws Exception {
        backendResponde(HttpMethod.POST, "/buscarProyectos", "[" + PROYECTO + "]");

        mvc.perform(post("/consultarProyectos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Proyecto A")));
    }

    // ---------- 10: eliminarProyecto ----------

    @Test
    void eliminarProyecto_ok_llamaDeleteYRedirigeAInicio() throws Exception {
        backendResponde(HttpMethod.DELETE, "/deleteProyecto", "");

        mvc.perform(post("/eliminarProyecto").contentType(JSON).content(PROYECTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/inicio*"));
    }

    // ---------- 11: actualizarProyecto ----------

    @Test
    void actualizarProyecto_consultaElProyectoPorId() throws Exception {
        backendResponde(HttpMethod.GET, "/buscarProyecto?id_proyecto=1", PROYECTO);

        mvc.perform(post("/actualizarProyecto").contentType(JSON).content("{\"id_proyecto\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Proyecto A")));
    }

    // ---------- 12: verificarLogin (exito y fracaso) ----------

    @Test
    void verificarLogin_credencialesCorrectas_devuelveElUsuario() throws Exception {
        backendResponde(HttpMethod.POST, "/getUsuarioLogin", USUARIO);

        mvc.perform(post("/verificarLogin").contentType(JSON)
                        .content("{\"correo_electronico\":\"ana@cbtis.mx\",\"contrasena\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void verificarLogin_credencialesIncorrectas_devuelveNombre000() throws Exception {
        backendResponde(HttpMethod.POST, "/getUsuarioLogin", USUARIO_NO_EXISTE);

        mvc.perform(post("/verificarLogin").contentType(JSON)
                        .content("{\"correo_electronico\":\"x@cbtis.mx\",\"contrasena\":\"mal\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("000"));
    }

    // ---------- 13: consultarUsuarios ----------

    @Test
    void consultarUsuarios_devuelveLaListaDelBackend() throws Exception {
        backendResponde(HttpMethod.POST, "/buscarUsuarios", "[" + USUARIO + "]");

        mvc.perform(post("/consultarUsuarios"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ana@cbtis.mx")));
    }

    // ---------- 14: obtenerTareas ----------

    @Test
    void obtenerTareas_devuelveLasTareasDelProyecto() throws Exception {
        backendResponde(HttpMethod.POST, "/getTarea", "[" + TAREA + "]");

        mvc.perform(post("/obtenerTareas").contentType(JSON).content("{\"id_proyecto\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tarea A")));
    }

    // ---------- 15: obtenerComentarios ----------

    @Test
    void obtenerComentarios_devuelveLosComentariosDeLaTarea() throws Exception {
        backendResponde(HttpMethod.POST, "/getComentario", "[" + COMENTARIO + "]");

        mvc.perform(post("/obtenerComentarios").contentType(JSON).content("{\"id_tarea\":4}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comentario A")));
    }

    // ---------- 16: proyecto (GET) ----------

    @Test
    void proyecto_cargaUsuariosYModelo() throws Exception {
        backendResponde(HttpMethod.POST, "/buscarUsuarios", "[" + USUARIO + "]");

        mvc.perform(get("/proyecto"))
                .andExpect(status().isOk())
                .andExpect(view().name("proyecto"))
                .andExpect(model().attributeExists("tarea", "comentarios", "listaUsuarios"));
    }

    // ---------- 17: tarea (GET) ----------

    @Test
    void tarea_devuelveLaVistaProyecto_conTareas() throws Exception {
        backendOpcional(HttpMethod.POST, "/buscarTareas", "[" + TAREA + "]");

        mvc.perform(get("/tarea"))
                .andExpect(status().isOk())
                .andExpect(view().name("proyecto"))
                .andExpect(model().attributeExists("tareas"));
    }

    // ---------- 18: saveTarea ----------

    @Test
    void saveTarea_ok_enviaAlBackend_yRedirigeAProyecto() throws Exception {
        backendResponde(HttpMethod.POST, "/addTareas", "1");

        mvc.perform(post("/saveTarea").param("nombre", "Tarea A").param("id_proyecto", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/proyecto*"));
    }

    // ---------- 19: saveComentario ----------

    @Test
    void saveComentario_ok_enviaAlBackend() throws Exception {
        backendResponde(HttpMethod.POST, "/addComentario", "1");

        mvc.perform(post("/saveComentario").contentType(JSON).content(COMENTARIO))
                .andExpect(status().isOk());
    }

    // ---------- 20: actualizarTarea ----------

    @Test
    void actualizarTarea_consultaLaTareaPorId() throws Exception {
        backendResponde(HttpMethod.GET, "/buscarTarea?id_tarea=4", TAREA);

        mvc.perform(post("/actualizarTarea").contentType(JSON).content("{\"id_tarea\":4}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tarea A")));
    }

    // ---------- 21: eliminarTarea ----------

    @Test
    void eliminarTarea_ok_llamaDeleteYRedirigeAProyecto() throws Exception {
        backendResponde(HttpMethod.DELETE, "/deleteTareas", "");

        mvc.perform(post("/eliminarTarea").contentType(JSON).content(TAREA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/proyecto*"));
    }

    // ---------- 22: verificarTarea ----------

    @Test
    void verificarTarea_devuelveElResultadoDelBackend() throws Exception {
        backendResponde(HttpMethod.POST, "/addTareas", "1");

        mvc.perform(post("/verificarTarea").contentType(JSON).content(TAREA))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    // ---------- 23: cambiosTareas ----------

    @Test
    void cambiosTareas_devuelveElResultadoDelBackend() throws Exception {
        backendResponde(HttpMethod.POST, "/addTareas", "1");

        mvc.perform(post("/cambiosTareas").contentType(JSON).content(TAREA))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    // ---------- 24: consultarTareas ----------

    @Test
    void consultarTareas_devuelveLaListaDelBackend() throws Exception {
        backendResponde(HttpMethod.POST, "/buscarTareas", "[" + TAREA + "]");

        mvc.perform(post("/consultarTareas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tarea A")));
    }

    // ---------- 25: consultarComentarios ----------

    @Test
    void consultarComentarios_devuelveLaListaDelBackend() throws Exception {
        backendResponde(HttpMethod.POST, "/buscarComentarios", "[" + COMENTARIO + "]");

        mvc.perform(post("/consultarComentarios"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comentario A")));
    }

    // ---------- CASOS DE FRACASO: el backend responde 500 ----------

    static Stream<Arguments> casosBackendCaido() {
        return Stream.of(
                caso("saveUsuario", HttpMethod.POST, "/addUsuario",
                        () -> post("/saveUsuario").param("nombre", "Ana")),
                caso("saveProyecto", HttpMethod.POST, "/addProyecto",
                        () -> post("/saveProyecto").param("nombre", "P")),
                caso("consultarProyectos", HttpMethod.POST, "/buscarProyectos",
                        () -> post("/consultarProyectos")),
                caso("eliminarProyecto", HttpMethod.DELETE, "/deleteProyecto",
                        () -> post("/eliminarProyecto").contentType(JSON).content(PROYECTO)),
                caso("actualizarProyecto", HttpMethod.GET, "/buscarProyecto?id_proyecto=1",
                        () -> post("/actualizarProyecto").contentType(JSON).content("{\"id_proyecto\":1}")),
                caso("verificarLogin", HttpMethod.POST, "/getUsuarioLogin",
                        () -> post("/verificarLogin").contentType(JSON).content("{\"correo_electronico\":\"a\"}")),
                caso("consultarUsuarios", HttpMethod.POST, "/buscarUsuarios",
                        () -> post("/consultarUsuarios")),
                caso("obtenerTareas", HttpMethod.POST, "/getTarea",
                        () -> post("/obtenerTareas").contentType(JSON).content("{\"id_proyecto\":1}")),
                caso("obtenerComentarios", HttpMethod.POST, "/getComentario",
                        () -> post("/obtenerComentarios").contentType(JSON).content("{\"id_tarea\":4}")),
                caso("saveTarea", HttpMethod.POST, "/addTareas",
                        () -> post("/saveTarea").param("nombre", "T")),
                caso("saveComentario", HttpMethod.POST, "/addComentario",
                        () -> post("/saveComentario").contentType(JSON).content(COMENTARIO)),
                caso("actualizarTarea", HttpMethod.GET, "/buscarTarea?id_tarea=4",
                        () -> post("/actualizarTarea").contentType(JSON).content("{\"id_tarea\":4}")),
                caso("eliminarTarea", HttpMethod.DELETE, "/deleteTareas",
                        () -> post("/eliminarTarea").contentType(JSON).content(TAREA)),
                caso("verificarTarea", HttpMethod.POST, "/addTareas",
                        () -> post("/verificarTarea").contentType(JSON).content(TAREA)),
                caso("cambiosTareas", HttpMethod.POST, "/addTareas",
                        () -> post("/cambiosTareas").contentType(JSON).content(TAREA)),
                caso("consultarTareas", HttpMethod.POST, "/buscarTareas",
                        () -> post("/consultarTareas")),
                caso("consultarComentarios", HttpMethod.POST, "/buscarComentarios",
                        () -> post("/consultarComentarios")));
    }

    private static Arguments caso(String nombre, HttpMethod metodo, String ruta, Supplier<RequestBuilder> peticion) {
        return Arguments.of(nombre, metodo, ruta, peticion);
    }

    @ParameterizedTest(name = "{0}: si el backend responde 500, el error se propaga")
    @MethodSource("casosBackendCaido")
    void backendCaido_propagaElError(String nombre, HttpMethod metodo, String ruta,
                                     Supplier<RequestBuilder> peticion) {
        backend.expect(requestTo(API + ruta)).andExpect(method(metodo)).andRespond(withServerError());

        Exception ex = assertThrows(Exception.class, () -> mvc.perform(peticion.get()));

        assertInstanceOf(HttpServerErrorException.class, ex.getCause(),
                "En " + nombre + " el error del backend debia propagarse");
    }
}
