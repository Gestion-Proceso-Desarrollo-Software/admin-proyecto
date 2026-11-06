package com.mx.cbtis.gestor.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.ProtectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas unitarias con Mockito para validar el comportamiento defensivo de los 5 controladores
 * cuando reciben una entidad nula (rama `entidad != null == false`), verificando que retornen
 * flag = 0 y que no interactuen con el repositorio.
 */
@ExtendWith(MockitoExtension.class)
class ControladoresNullSafetyMockitoTest {

    @Mock
    private AsignacionesRepository asignacionesRepository;
    @InjectMocks
    private AsignacionesController asignacionesController;

    @Mock
    private ComentariosRepository comentariosRepository;
    @InjectMocks
    private ComentariosController comentariosController;

    @Mock
    private ProtectosRepository proyectosRepository;
    @InjectMocks
    private ProyectosController proyectosController;

    @Mock
    private TareasRepository tareasRepository;
    @InjectMocks
    private TareasController tareasController;

    @Mock
    private UsuarioRepository usuarioRepository;
    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    void asignaciones_cuandoEntidadEsNull_devuelveCeroYNoLlamaAlRepositorio() {
        assertEquals(0, asignacionesController.addAsignacion(null));
        assertEquals(0, asignacionesController.updateAsignacion(null));
        assertEquals(0, asignacionesController.deleteAsignacion(null));
        verifyNoInteractions(asignacionesRepository);
    }

    @Test
    void comentarios_cuandoEntidadEsNull_devuelveCeroYNoLlamaAlRepositorio() {
        assertEquals(0, comentariosController.addComentario(null));
        assertEquals(0, comentariosController.updateComentario(null));
        assertEquals(0, comentariosController.deleteComentario(null));
        verifyNoInteractions(comentariosRepository);
    }

    @Test
    void proyectos_cuandoEntidadEsNull_devuelveCeroYNoLlamaAlRepositorio() {
        assertEquals(0, proyectosController.addProyecto(null));
        assertEquals(0, proyectosController.updateProyecto(null));
        assertEquals(0, proyectosController.deleteProyecto(null));
        verifyNoInteractions(proyectosRepository);
    }

    @Test
    void tareas_cuandoEntidadEsNull_devuelveCeroYNoLlamaAlRepositorio() {
        assertEquals(0, tareasController.addTareas(null));
        assertEquals(0, tareasController.updateTareas(null));
        assertEquals(0, tareasController.deleteTareas(null));
        verifyNoInteractions(tareasRepository);
    }

    @Test
    void usuario_cuandoEntidadEsNull_devuelveCeroYNoLlamaAlRepositorio() {
        assertEquals(0, usuarioController.addUsuario(null));
        assertEquals(0, usuarioController.updateUsuario(null));
        assertEquals(0, usuarioController.deleteUsuario(null));
        verifyNoInteractions(usuarioRepository);
    }
}
