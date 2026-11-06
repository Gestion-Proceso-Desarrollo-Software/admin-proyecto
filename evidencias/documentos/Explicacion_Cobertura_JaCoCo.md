# Mejora de cobertura de pruebas (JaCoCo)

## 1. Situación inicial
En la fase inicial del proyecto, la cobertura de código en ambos microservicios se encontraba en niveles críticos, prácticamente inexistentes. De acuerdo con el panel general de SonarQube (*SonarQube Overview*) capturado antes de la intervención, los proyectos presentaban las siguientes métricas de cobertura global:

- **Admin Proyectos Backend (`Ejercicio5MyAPIGestor`):** Cobertura global del **5.1%** (sobre un total de 768 líneas de código analizadas). El desglose específico de ramas, líneas y métodos no es visible en la tarjeta general de la captura.
- **Admin Proyectos Frontend (`Ejercicio5Cliente`):** Cobertura global del **0.7%** (sobre un total de 595 líneas de código analizadas). El desglose específico de ramas, líneas y métodos no es visible en la tarjeta general de la captura.

![Figura 1 – Antes con coverage bajo](../capturas/Antes con coverage bajo.png)

Esta situación evidenciaba que las pruebas unitarias existentes no cubrían los flujos reales de la aplicación, dejando la lógica de controladores, acceso a datos y manejo de entidades sin verificación automatizada en el ciclo de integración continua.

## 2. Causa de la cobertura baja
Al revisar los reportes y la estructura original del código, se identificó que la causa raíz de la baja cobertura radicaba en que únicamente existían las clases de prueba generadas por defecto por el arquetipo de Spring Initializr:
- En el backend: `Ejercicio5MyApiGestorApplicationTests.java`, la cual únicamente ejecutaba una prueba de carga de contexto (`contextLoads()`).
- En el frontend: `Ejercicio5ClienteApplicationTests.java`, igualmente limitada al método `contextLoads()`.

Ninguno de los cinco controladores REST del backend (`AsignacionesController`, `ComentariosController`, `ProyectosController`, `TareasController` y `UsuarioController`), ni las consultas nativas del repositorio `ConsultasRepository`, ni los métodos de los modelos de dominio contaban con pruebas unitarias o de integración. De igual forma, en el frontend, la clase principal `GestorController` (que gestiona todas las llamadas HTTP remotas mediante `RestTemplate` hacia la API del backend) y sus respectivos modelos de datos se encontraban completamente desprovistos de pruebas automatizadas.

## 3. Acciones realizadas
Para subsanar las deficiencias identificadas y alcanzar los estándares exigidos por la rúbrica de evaluación, se diseñó e implementó un conjunto exhaustivo de pruebas automatizadas en ambos microservicios, empleando Spring Boot Test, JUnit 5, MockMvc, Mockito, MockRestServiceServer y una base de datos H2 en memoria:

### Backend (`Ejercicio5MyAPIGestor`) – 65 pruebas automatizadas (0 fallos, 0 errores)
Se desarrollaron las siguientes clases de prueba que cubren todos los métodos públicos y casos relevantes:
- `AsignacionesControllerTest`: Verificación completa de los endpoints REST de asignación de tareas a usuarios (GET, POST, PUT, DELETE), validando respuestas HTTP 200, 404 y manejo de excepciones.
- `ComentariosControllerTest`: Pruebas sobre la creación, consulta y asociación de comentarios en tareas y proyectos.
- `ProyectosControllerTest`: Pruebas del ciclo de vida de proyectos (registro, consulta por identificador, listados y actualización).
- `TareasControllerTest`: Pruebas sobre creación, actualización de estados de tareas y asignación.
- `UsuarioControllerTest`: Pruebas de administración, consulta y autenticación de usuarios.
- `ConsultasRepositoryTest`: Verificación sobre base de datos H2 en memoria de las 7 consultas nativas personalizadas definidas en el repositorio.
- `ControladoresNullSafetyMockitoTest`: Pruebas de robustez para entradas nulas y casos de borde.
- `ModeloTest` y `PojoTester`: Verificación exhaustiva de constructores, getters, setters, `equals`, `hashCode` y `toString` de todas las entidades de dominio.

*Total en backend:* **65 pruebas ejecutadas, 0 fallos, 0 errores** (confirmado en el reporte de Surefire y en la evidencia `R6-07_tabla-pruebas-proyectos.png`).

### Frontend (`Ejercicio5Cliente`) – 49 pruebas automatizadas (0 fallos, 0 errores)
Se desarrollaron suites de pruebas centradas en la integración web y la contingencia de servicios:
- `GestorControllerTest`: Pruebas unitarias y de integración utilizando `MockMvc` standalone y `MockRestServiceServer` para simular las respuestas del backend remoto, evaluando exhaustivamente todos los métodos públicos del controlador (`listarProyectos`, `verDetalle`, `crearProyecto`, `actualizarEstadoTarea`, `gestionarUsuarios`, etc.), cubriendo tanto los flujos exitosos (HTTP 200) como los escenarios de fallo del servicio externo (HTTP 500).
- `ModeloTest` y `PojoTester`: Pruebas de integridad y cobertura sobre todos los modelos y DTOs del cliente web.

*Total en frontend:* **49 pruebas ejecutadas, 0 fallos, 0 errores** (confirmado en el reporte de Surefire y en la evidencia `R6-07_tabla-pruebas-proyectos.png`).

*Total consolidado del proyecto:* **114 pruebas automatizadas con 100% de éxito**.

## 4. Resultado
Tras la ejecución de `mvn clean test jacoco:report`, los reportes generados por JaCoCo certifican una cobertura total en ambos microservicios, cumpliendo holgadamente el criterio de evaluación:

- **Backend (`Ejercicio5MyAPIGestor`):**
  - **Cobertura de Métodos:** **100.0%** (101 de 101 métodos cubiertos).
  - **Cobertura de Líneas:** **100.0%** (268 de 268 líneas cubiertas).
  - **Cobertura de Instrucciones:** **100.0%** (736 de 736 instrucciones cubiertas).
  - **Cobertura de Ramas (*Branches*):** **100.0%** (42 de 42 ramas cubiertas).
- **Frontend (`Ejercicio5Cliente`):**
  - **Cobertura de Métodos:** **100.0%** (94 de 94 métodos cubiertos).
  - **Cobertura de Líneas:** **100.0%** (254 de 254 líneas cubiertas).
  - **Cobertura de Instrucciones:** **100.0%** (835 de 835 instrucciones cubiertas).
  - **Cobertura de Ramas (*Branches*):** N/A (0 ramas condicionales en el código evaluado).

![Figura 2 – Tabla comparativa de cobertura](../capturas/R6-06_tabla-cobertura-proyectos.png)

## 5. Justificación técnica
### Relevancia de la cobertura de métodos al 100%
En una arquitectura basada en microservicios, garantizar que el 100% de los métodos de controladores, servicios, repositorios y modelos sean ejecutados durante las pruebas asegura que ninguna ruta de ejecución ni contrato de API quede sin verificar. Esta métrica confirma que todos los puntos de entrada expuestos a los clientes y usuarios finales han sido probados ante condiciones válidas y de error, minimizando el riesgo de fallos en producción y asegurando la estabilidad del pipeline CI/CD.

### Justificación de las exclusiones en JaCoCo
En los archivos `pom.xml` de ambos proyectos, dentro de la configuración del `jacoco-maven-plugin` (versión 0.8.12), se configuró la exclusión explícita del patrón:
`<exclude>**/*Application.class</exclude>`

Esta exclusión aplica a las clases:
- `Ejercicio5MyApiGestorApplication.class` (en backend)
- `Ejercicio5ClienteApplication.class` (en frontend)

**Justificación:** Estas clases corresponden exclusivamente a las clases de arranque (*bootstrap*) del framework Spring Boot, cuyo único contenido es el método canónico `public static void main(String[] args)` con la invocación `SpringApplication.run(...)`. Dichas clases no contienen lógica de negocio, bifurcaciones condicionales, reglas de validación ni procesamiento de datos. Excluirlas es una práctica estándar recomendada en la industria para evitar métricas distorsionadas por código de inicialización de la plataforma, manteniendo el foco de la auditoría de calidad estrictamente en el código de aplicación desarrollado. Ningún controlador, servicio, repositorio o entidad fue excluido.

## 6. Verificación
La veracidad de los datos y la efectividad de las pruebas pueden ser corroboradas directamente en los siguientes artefactos:
1. **Reportes HTML de JaCoCo:** Archivos `target/site/jacoco/index.html` generados en `Ejercicio5MyAPIGestor` y `Ejercicio5Cliente`.
2. **Reportes CSV de JaCoCo:** Archivos `target/site/jacoco/jacoco.csv` en cada microservicio, que contienen el desglose cuantitativo instrucción por instrucción y método por método.
3. **Evidencias gráficas del repositorio:**
   - `R6-06_tabla-cobertura-proyectos.png`: Muestra comparativa de ambos reportes JaCoCo con 100% de cobertura de métodos.
   - `P2-03_jacoco-cobertura-metodos-backend.png`: Detalle del backend con 101/101 métodos cubiertos.
   - `P2-04_jacoco-cobertura-metodos-frontend.png`: Detalle del frontend con 94/94 métodos cubiertos.
   - `R6-07_tabla-pruebas-proyectos.png`: Reporte consolidado de Surefire certificando 114 pruebas exitosas (65 en backend y 49 en frontend) con 0 fallos.
4. **Pipeline CI de Jenkins:** Ejecución exitosa de la etapa de pruebas unitarias y cobertura en los pipelines configurados.
