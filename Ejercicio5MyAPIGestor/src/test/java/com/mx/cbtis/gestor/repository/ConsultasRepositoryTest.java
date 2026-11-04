package com.mx.cbtis.gestor.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

/** Prueba las 3 consultas nativas (@Query) de los repositorios contra H2 (perfil "test"). */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ConsultasRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private ComentariosRepository comentarios;
    @Autowired
    private TareasRepository tareas;
    @Autowired
    private UsuarioRepository usuarios;

    private void guardarComentario(int tarea, String texto) {
        Comentarios c = new Comentarios();
        c.setId_usuario("1");
        c.setId_tarea(tarea);
        c.setTexto_comentario(texto);
        c.setFecha_hora("2026-10-28 10:00");
        em.persist(c);
    }

    private void guardarTarea(int proyecto, String nombre) {
        Tareas t = new Tareas();
        t.setNombre(nombre);
        t.setDescripcion("d");
        t.setFecha_vencimiento("2026-11-30");
        t.setLista("Por hacer");
        t.setEstado("Pendiente");
        t.setId_proyecto(proyecto);
        em.persist(t);
    }

    @Test
    void comentarios_findByTarea_devuelveSoloLaTareaPedida() {
        guardarComentario(5, "a");
        guardarComentario(5, "b");
        guardarComentario(6, "c");
        em.flush();
        em.clear();

        List<Comentarios> resultado = comentarios.findByTarea(5);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(c -> c.getId_tarea() == 5));
    }

    @Test
    void comentarios_findByTarea_sinResultados_devuelveListaVacia() {
        guardarComentario(5, "a");
        em.flush();

        assertTrue(comentarios.findByTarea(999).isEmpty());
    }

    @Test
    void tareas_findByIdProyecto_devuelveSoloElProyectoPedido() {
        guardarTarea(1, "t1");
        guardarTarea(1, "t2");
        guardarTarea(2, "t3");
        em.flush();
        em.clear();

        List<Tareas> resultado = tareas.findByIdProyecto(1);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(t -> t.getId_proyecto() == 1));
    }

    @Test
    void tareas_findByIdProyecto_sinResultados_devuelveListaVacia() {
        guardarTarea(1, "t1");
        em.flush();

        assertTrue(tareas.findByIdProyecto(999).isEmpty());
    }

    private void guardarUsuario() {
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setCorreo_electronico("ana@cbtis.mx");
        u.setContrasena("clave-ana");
        u.setRol("alumno");
        u.setImagen_usuario("foto.png");
        em.persist(u);
        em.flush();
        em.clear();
    }

    @Test
    void usuario_findByCorreoPass_credencialesCorrectas_devuelveElUsuario() {
        guardarUsuario();

        Optional<Usuario> resultado = usuarios.findByCorreoPass("ana@cbtis.mx", "clave-ana");

        assertTrue(resultado.isPresent());
        assertEquals("Ana", resultado.get().getNombre());
    }

    @Test
    void usuario_findByCorreoPass_contrasenaIncorrecta_devuelveVacio() {
        guardarUsuario();

        assertTrue(usuarios.findByCorreoPass("ana@cbtis.mx", "mal").isEmpty());
    }

    @Test
    void usuario_findByCorreoPass_correoInexistente_devuelveVacio() {
        guardarUsuario();

        assertTrue(usuarios.findByCorreoPass("otro@cbtis.mx", "clave-ana").isEmpty());
    }
}
