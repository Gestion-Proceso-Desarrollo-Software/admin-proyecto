package com.mx.cbtis.gestor.modelo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mx.cbtis.gestor.testutil.PojoTester;
import org.junit.jupiter.api.Test;

/**
 * Entidades JPA: constructores, getters y setters (sin exclusiones en JaCoCo).
 */
class ModeloTest {

    @Test
    void asignaciones() throws Exception {
        Asignaciones porReflexion = PojoTester.probar(Asignaciones.class);
        Asignaciones entidad = new Asignaciones(1, 2, 3, "2026-11-06");

        assertAll(
                () -> assertNotNull(porReflexion),
                () -> assertEquals(1, entidad.getId_asignacion()),
                () -> assertEquals(2, entidad.getId_usuario()),
                () -> assertEquals(3, entidad.getId_tarea()),
                () -> assertEquals("2026-11-06", entidad.getFecha_asignacion())
        );
    }

    @Test
    void comentarios() throws Exception {
        Comentarios porReflexion = PojoTester.probar(Comentarios.class);
        Comentarios entidad = new Comentarios(10, "usr-1", 5, "Revision lista", "2026-11-06 10:00");

        assertAll(
                () -> assertNotNull(porReflexion),
                () -> assertEquals(10, entidad.getId_comentario()),
                () -> assertEquals("usr-1", entidad.getId_usuario()),
                () -> assertEquals(5, entidad.getId_tarea()),
                () -> assertEquals("Revision lista", entidad.getTexto_comentario()),
                () -> assertEquals("2026-11-06 10:00", entidad.getFecha_hora())
        );
    }

    @Test
    void proyectos() throws Exception {
        Proyectos porReflexion = PojoTester.probar(Proyectos.class);
        Proyectos entidad = new Proyectos(
                4, "Sistema CI", "Pipeline completo", "2026-10-01", "2026-11-06", "Activo", "azul"
        );

        assertAll(
                () -> assertNotNull(porReflexion),
                () -> assertEquals(4, entidad.getId_proyecto()),
                () -> assertEquals("Sistema CI", entidad.getNombre()),
                () -> assertEquals("Pipeline completo", entidad.getDescripcion()),
                () -> assertEquals("2026-10-01", entidad.getFecha_inicio()),
                () -> assertEquals("2026-11-06", entidad.getFecha_finalizacion()),
                () -> assertEquals("Activo", entidad.getEstado_proyecto()),
                () -> assertEquals("azul", entidad.getFondo())
        );
    }

    @Test
    void tareas() throws Exception {
        Tareas porReflexion = PojoTester.probar(Tareas.class);
        Tareas entidad = new Tareas(
                8, "Configurar Sonar", "Conectar JaCoCo XML", "2026-11-06", "En proceso", "Activa", 4
        );

        assertAll(
                () -> assertNotNull(porReflexion),
                () -> assertEquals(8, entidad.getId_tarea()),
                () -> assertEquals("Configurar Sonar", entidad.getNombre()),
                () -> assertEquals("Conectar JaCoCo XML", entidad.getDescripcion()),
                () -> assertEquals("2026-11-06", entidad.getFecha_vencimiento()),
                () -> assertEquals("En proceso", entidad.getLista()),
                () -> assertEquals("Activa", entidad.getEstado()),
                () -> assertEquals(4, entidad.getId_proyecto())
        );
    }

    @Test
    void usuario() throws Exception {
        Usuario porReflexion = PojoTester.probar(Usuario.class);
        Usuario entidad = new Usuario(
                3, "Enrique", "enrique@cbtis.mx", "secreto123", "admin", "avatar.png"
        );

        assertAll(
                () -> assertNotNull(porReflexion),
                () -> assertEquals(3, entidad.getId_usuario()),
                () -> assertEquals("Enrique", entidad.getNombre()),
                () -> assertEquals("enrique@cbtis.mx", entidad.getCorreo_electronico()),
                () -> assertEquals("secreto123", entidad.getContrasena()),
                () -> assertEquals("admin", entidad.getRol()),
                () -> assertEquals("avatar.png", entidad.getImagen_usuario())
        );
    }
}
