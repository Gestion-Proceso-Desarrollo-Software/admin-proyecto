package com.mx.cbtis.gestor.modelo;

import com.mx.cbtis.gestor.testutil.PojoTester;
import org.junit.jupiter.api.Test;

/** Entidades JPA: constructores, getters y setters (sin exclusiones en JaCoCo). */
class ModeloTest {

    @Test
    void asignaciones() throws Exception {
        PojoTester.probar(Asignaciones.class);
    }

    @Test
    void comentarios() throws Exception {
        PojoTester.probar(Comentarios.class);
    }

    @Test
    void proyectos() throws Exception {
        PojoTester.probar(Proyectos.class);
    }

    @Test
    void tareas() throws Exception {
        PojoTester.probar(Tareas.class);
    }

    @Test
    void usuario() throws Exception {
        PojoTester.probar(Usuario.class);
    }
}
