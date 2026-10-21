# Plan de pruebas y endpoints

Este proyecto tiene dos aplicaciones Spring Boot: `Ejercicio5MyAPIGestor` (API y persistencia) y `Ejercicio5Cliente` (interfaz web que consume la API). Esta guía describe las rutas que existen actualmente y propone pruebas automatizadas y manuales.

## Preparación

Abre dos terminales desde la carpeta del repositorio.

En la primera, inicia la API:

```powershell
Set-Location ".\Ejercicio5MyAPIGestor"
.\mvnw.cmd spring-boot:run
```

La API usa el puerto `8081` y, fuera de las pruebas automatizadas, requiere MariaDB disponible con la base `dbgestor` y las credenciales configuradas en `src\main\resources\application.properties`.

En la segunda, inicia el cliente web:

```powershell
Set-Location ".\Ejercicio5MyAPIGestor"
.\mvnw.cmd -f ..\Ejercicio5Cliente\pom.xml spring-boot:run
```

La interfaz se sirve en <http://localhost:8080/>. Para probarla, deja las dos aplicaciones corriendo. Para las pruebas de API basta con iniciar la API.

## Pruebas automatizadas existentes

Ejecuta estos comandos desde la carpeta `Ejercicio5MyAPIGestor`:

```powershell
# Compila y ejecuta las pruebas de la API.
.\mvnw.cmd test

# Ejecuta las pruebas del cliente web.
.\mvnw.cmd -f ..\Ejercicio5Cliente\pom.xml test
```

Actualmente cada módulo tiene una prueba `contextLoads`: comprueba que Spring puede iniciar el contexto, pero no verifica reglas de negocio ni los endpoints. La prueba de contexto de la API usa H2 en memoria mediante el perfil `test`; los datos se eliminan al terminar. Las pruebas manuales de las siguientes secciones llaman a la API real y guardan datos en MariaDB.

## Endpoints de la API

Base URL: `http://localhost:8081`. Este catálogo incluye los **28 endpoints** declarados en los controladores de la API. En las peticiones `GET`, los datos van en la URL y no se envía cuerpo. Las rutas de listado son `POST` y no reciben cuerpo. Para probar los ejemplos, reemplaza los IDs de muestra (`1`) por IDs que existan en tu base de datos; las altas generan sus propios IDs.

El catálogo completo en formato JSON, con método HTTP, URL, cuerpo de ejemplo y respuesta esperada para cada endpoint, está en [`endpoints-backend.json`](endpoints-backend.json). Se actualizó con los IDs existentes de `dbgestor` que compartiste; los IDs `0` en actualización/eliminación son marcadores y deben cambiarse por registros de prueba para evitar modificar o borrar los datos ya guardados. La tabla de asignaciones está vacía, así que crea una antes de probar operaciones por ID.

| Recurso | Método y ruta | Cuerpo JSON / parámetros | Resultado esperado |
|---|---|---|---|
| Proyectos | `POST /addProyecto` | Proyecto nuevo, ver JSON de proyecto | Guarda y responde `1`. |
| Proyectos | `POST /updateProyecto` | Proyecto con `id_proyecto`, ver JSON de actualización | Guarda el objeto y responde `1`. |
| Proyectos | `GET /buscarProyecto?id_proyecto=1` | Sin cuerpo; query `id_proyecto` | Devuelve el proyecto o un objeto vacío si no existe. |
| Proyectos | `POST /buscarProyectos` | Sin cuerpo | Devuelve todos los proyectos. |
| Proyectos | `DELETE /deleteProyecto` | `{"id_proyecto":1}` | Elimina y responde `1`. |
| Usuarios | `POST /addUsuario` | Usuario nuevo, ver JSON de usuario | Guarda y responde `1`. |
| Usuarios | `POST /updateUsuario` | Usuario con `id_usuario`, ver JSON de actualización | Guarda el objeto y responde `1`. |
| Usuarios | `GET /buscarUsuario?id=1` | Sin cuerpo; query `id` | Devuelve el usuario o un objeto vacío si no existe. |
| Usuarios | `POST /buscarUsuarios` | Sin cuerpo | Devuelve todos los usuarios. |
| Usuarios | `DELETE /deleteUsuario` | `{"id_usuario":1}` | Elimina y responde `1`. |
| Usuarios | `POST /getUsuarioLogin` | `{"correo_electronico":"prueba@example.test","contrasena":"ClavePrueba123"}` | Devuelve el usuario; si no coincide, devuelve `"nombre":"000"`. |
| Tareas | `POST /addTareas` | Tarea nueva, ver JSON de tarea | Guarda y responde `1`. |
| Tareas | `POST /updateTareas` | Tarea con `id_tarea`, ver JSON de actualización | Guarda el objeto y responde `1`. |
| Tareas | `GET /buscarTarea?id_tarea=1` | Sin cuerpo; query `id_tarea` | Devuelve la tarea o un objeto vacío si no existe. |
| Tareas | `POST /getTarea` | `{"id_proyecto":1}` | Devuelve las tareas del proyecto. |
| Tareas | `POST /buscarTareas` | Sin cuerpo | Devuelve todas las tareas. |
| Tareas | `DELETE /deleteTareas` | `{"id_tarea":1}` | Elimina y responde `1`. |
| Comentarios | `POST /addComentario` | Comentario nuevo, ver JSON de comentario | Guarda y responde `1`. |
| Comentarios | `POST /updateComentario` | Comentario con `id_comentario`, ver JSON de actualización | Guarda el objeto y responde `1`. |
| Comentarios | `GET /buscarComentario?id=1` | Sin cuerpo; query `id` | Devuelve el comentario o un objeto vacío si no existe. |
| Comentarios | `POST /getComentario` | `{"id_tarea":1}` | Devuelve los comentarios de la tarea. |
| Comentarios | `POST /buscarComentarios` | Sin cuerpo | Devuelve todos los comentarios. |
| Comentarios | `DELETE /deleteComentario` | `{"id_comentario":1}` | Elimina y responde `1`. |
| Asignaciones | `POST /addAsignacion` | Asignación nueva, ver JSON de asignación | Guarda y responde `1`. |
| Asignaciones | `POST /updateAsignacion` | Asignación con `id_asignacion`, ver JSON de actualización | Guarda el objeto y responde `1`. |
| Asignaciones | `GET /buscarAsignacion?id=1` | Sin cuerpo; query `id` | Devuelve la asignación o un objeto vacío si no existe. |
| Asignaciones | `POST /buscarAsignaciones` | Sin cuerpo | Devuelve todas las asignaciones. |
| Asignaciones | `DELETE /deleteAsignacion` | `{"id_asignacion":1}` | Elimina y responde `1`. |

### Cuerpos JSON para copiar y probar

Usa `Content-Type: application/json` en las solicitudes con cuerpo. Los campos ID se omiten al crear porque se generan automáticamente; en actualizaciones y eliminaciones debes usar un registro existente.

**Proyectos** — `POST /addProyecto`:

```json
{
  "nombre": "Proyecto de prueba",
  "descripcion": "Registro para probar la API",
  "fecha_inicio": "2026-10-01",
  "fecha_finalizacion": "2026-12-01",
  "estado_proyecto": "Activo",
  "fondo": ""
}
```

Para `POST /updateProyecto`, envía el mismo objeto incluyendo `"id_proyecto": 1`. Para `DELETE /deleteProyecto`, el cuerpo mínimo es:

```json
{"id_proyecto": 1}
```

**Usuarios** — `POST /addUsuario`:

```json
{
  "nombre": "Usuario de prueba",
  "correo_electronico": "prueba@example.test",
  "contrasena": "ClaveSoloDePrueba123",
  "rol": "Estudiante",
  "imagen_usuario": "#336699"
}
```

Para `POST /updateUsuario`, incluye `"id_usuario": 1` en ese objeto. El inicio de sesión (`POST /getUsuarioLogin`) requiere:

```json
{
  "correo_electronico": "prueba@example.test",
  "contrasena": "ClaveSoloDePrueba123"
}
```

Para `DELETE /deleteUsuario`, envía `{"id_usuario": 1}`.

**Tareas** — `POST /addTareas`:

```json
{
  "nombre": "Tarea de prueba",
  "descripcion": "Verificar alta y consulta",
  "fecha_vencimiento": "2026-12-01",
  "lista": "pendientes",
  "estado": "primary",
  "id_proyecto": 1
}
```

Para `POST /updateTareas`, incluye `"id_tarea": 1`. Para `POST /getTarea`, el cuerpo mínimo es `{"id_proyecto": 1}`; para `DELETE /deleteTareas`, es `{"id_tarea": 1}`.

**Comentarios** — `POST /addComentario`:

```json
{
  "id_usuario": "prueba@example.test",
  "id_tarea": 1,
  "texto_comentario": "Comentario de prueba",
  "fecha_hora": "2026-10-01 12:00"
}
```

Para `POST /updateComentario`, incluye `"id_comentario": 1`. Para `POST /getComentario`, envía `{"id_tarea": 1}`; para `DELETE /deleteComentario`, envía `{"id_comentario": 1}`.

**Asignaciones** — `POST /addAsignacion`:

```json
{
  "id_usuario": 1,
  "id_tarea": 1,
  "fecha_asignacion": "2026-10-01"
}
```

Para `POST /updateAsignacion`, incluye `"id_asignacion": 1`. Para `DELETE /deleteAsignacion`, envía `{"id_asignacion": 1}`.

Las solicitudes a los endpoints de listado (`POST /buscarProyectos`, `/buscarUsuarios`, `/buscarTareas`, `/buscarComentarios` y `/buscarAsignaciones`) no llevan JSON: se envían sin cuerpo. Los nombres y tipos de los campos corresponden a los modelos actuales (por ejemplo, `id_comentario` y `id_tarea` son números; `id_usuario` en comentarios es texto).

## Flujo de prueba manual con PowerShell

Ejecuta los siguientes comandos en una terminal donde la API esté funcionando. Se crean registros de prueba en MariaDB; cambia el sufijo si repites el flujo. Usa una contraseña inventada solo para pruebas locales.

### Crear y consultar un proyecto

```powershell
$api = "http://localhost:8081"
$sufijo = Get-Date -Format "yyyyMMddHHmmss"
$nombreProyecto = "Prueba-$sufijo"
$proyecto = @{
  nombre = $nombreProyecto
  descripcion = "Proyecto temporal para pruebas"
  fecha_inicio = "2026-10-01"
  fecha_finalizacion = "2026-12-01"
  estado_proyecto = "Activo"
  fondo = ""
} | ConvertTo-Json

$resultado = Invoke-RestMethod -Uri "$api/addProyecto" -Method Post `
  -ContentType "application/json" -Body $proyecto
$resultado # Se espera 1

$lista = Invoke-RestMethod -Uri "$api/buscarProyectos" -Method Post
$proyectoGuardado = $lista | Where-Object nombre -eq $nombreProyecto |
  Select-Object -First 1
$proyectoGuardado
$idProyecto = $proyectoGuardado.id_proyecto

Invoke-RestMethod -Uri "$api/buscarProyecto?id_proyecto=$idProyecto"
```

Verifica que la respuesta individual tenga el mismo ID, nombre y descripción. La lista de tareas por proyecto se consulta así:

```powershell
Invoke-RestMethod -Uri "$api/getTarea" -Method Post `
  -ContentType "application/json" `
  -Body (@{ id_proyecto = $idProyecto } | ConvertTo-Json)
```

### Crear un usuario y comprobar el inicio de sesión

```powershell
$correo = "prueba$sufijo@example.test"
$usuario = @{
  nombre = "Usuario de prueba"
  correo_electronico = $correo
  contrasena = "ClaveSoloDePrueba123"
  rol = "Estudiante"
  imagen_usuario = "#336699"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$api/addUsuario" -Method Post `
  -ContentType "application/json" -Body $usuario # Se espera 1

$loginValido = @{
  correo_electronico = $correo
  contrasena = "ClaveSoloDePrueba123"
} | ConvertTo-Json
$usuarioEncontrado = Invoke-RestMethod -Uri "$api/getUsuarioLogin" -Method Post `
  -ContentType "application/json" -Body $loginValido
$idUsuario = $usuarioEncontrado.id_usuario

$loginInvalido = @{
  correo_electronico = $correo
  contrasena = "incorrecta"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$api/getUsuarioLogin" -Method Post `
  -ContentType "application/json" -Body $loginInvalido
```

El primer inicio de sesión debe devolver el usuario; el segundo debe devolver `"nombre": "000"`. No uses una contraseña personal: la aplicación actualmente compara y almacena la contraseña como texto.

### Pruebas adicionales de tareas y comentarios

Primero crea una tarea asociada al ID de proyecto obtenido arriba:

```powershell
$tarea = @{
  nombre = "Tarea de prueba $sufijo"
  descripcion = "Verificar alta y consulta"
  fecha_vencimiento = "2026-12-01"
  lista = "pendientes"
  estado = "primary"
  id_proyecto = $idProyecto
} | ConvertTo-Json
Invoke-RestMethod -Uri "$api/addTareas" -Method Post `
  -ContentType "application/json" -Body $tarea # Se espera 1

$tareas = Invoke-RestMethod -Uri "$api/getTarea" -Method Post `
  -ContentType "application/json" `
  -Body (@{ id_proyecto = $idProyecto } | ConvertTo-Json)
$tareaGuardada = $tareas | Where-Object nombre -eq "Tarea de prueba $sufijo" |
  Select-Object -First 1
$tareaGuardada
$idTarea = $tareaGuardada.id_tarea
```

Agrega y consulta un comentario de esa tarea:

```powershell
$comentario = @{
  id_usuario = $correo
  id_tarea = $idTarea
  texto_comentario = "Comentario de prueba"
  fecha_hora = "2026-10-01 12:00"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$api/addComentario" -Method Post `
  -ContentType "application/json" -Body $comentario # Se espera 1

Invoke-RestMethod -Uri "$api/getComentario" -Method Post `
  -ContentType "application/json" `
  -Body (@{ id_tarea = $idTarea } | ConvertTo-Json)
```

Para asignaciones, usa el ID numérico obtenido del usuario y el ID de la tarea:

```powershell
$asignacion = @{
  id_usuario = $idUsuario
  id_tarea = $idTarea
  fecha_asignacion = "2026-10-01"
} | ConvertTo-Json
Invoke-RestMethod -Uri "$api/addAsignacion" -Method Post `
  -ContentType "application/json" -Body $asignacion # Se espera 1
Invoke-RestMethod -Uri "$api/buscarAsignaciones" -Method Post
```

### Actualizar y eliminar

Para probar actualización, envía por `POST /updateProyecto` el objeto consultado con su `id_proyecto` y un campo modificado; luego vuelve a consultarlo con `GET /buscarProyecto?id_proyecto=...`. Para eliminar, envía por `DELETE /deleteProyecto` un JSON que incluya el ID del registro de prueba:

```powershell
$proyectoEliminar = @{ id_proyecto = $idProyecto } | ConvertTo-Json
Invoke-RestMethod -Uri "$api/deleteProyecto" -Method Delete `
  -ContentType "application/json" -Body $proyectoEliminar
```

La respuesta actual es `1`. Repite el patrón para `deleteUsuario`, `deleteTareas`, `deleteComentario` y `deleteAsignacion`, usando el nombre de propiedad ID del recurso. Elimina solo los registros creados para la prueba.

## Pruebas propuestas para automatizar con JUnit

Estas son pruebas pendientes; no se ejecutan automáticamente por el hecho de estar descritas aquí.

| Componente | Tipo de prueba | Casos sugeridos |
|---|---|---|
| Repositorios | `@DataJpaTest` con H2 | Guardar y recuperar cada entidad; filtrar tareas por `id_proyecto`; filtrar comentarios por `id_tarea`; buscar usuario por correo y contraseña. |
| Controladores de API | `@WebMvcTest` y repositorios simulados | Verificar verbo/ruta, parámetros y JSON; alta/edición/eliminación; lista vacía; consulta por ID existente e inexistente; credenciales válidas e inválidas. |
| API con persistencia | `@SpringBootTest` con H2 y `MockMvc` | Flujo alta → consulta → actualización → eliminación; verificar que filtros por proyecto/tarea no mezclen registros. |
| Controlador web | `@WebMvcTest` y cliente HTTP simulado | Confirmar que las vistas principales se renderizan y que las rutas proxy envían a la API el método, ruta y cuerpo esperados. |
| Interfaz JavaScript | Pruebas de navegador (por ejemplo, Selenium) | Validación de registro e inicio de sesión; errores de formulario; listar proyectos; crear tarea; ver comentarios; comprobar manejo de respuestas fallidas. |

Casos límite recomendados para los controladores: parámetros ausentes o no numéricos, JSON mal formado, cuerpo vacío, campos requeridos vacíos, IDs inexistentes y fallo de conexión con la API/base de datos. Antes de definirlos como pruebas aprobadas, conviene acordar el contrato esperado (por ejemplo, `400` para entrada inválida y `404` para un ID inexistente) e implementar la validación correspondiente.

## Rutas del cliente web

Las rutas del cliente usan `http://localhost:8080`. Las que llaman a la base de datos o a la API requieren que el servidor de `8081` esté disponible. Los formularios usan `application/x-www-form-urlencoded`; los endpoints marcados JSON esperan `Content-Type: application/json`.

| Método y ruta | Cuerpo / parámetros | Comportamiento actual |
|---|---|---|
| `GET /`, `GET /tutorial`, `GET /manual`, `GET /login` | Sin cuerpo | Renderizan sus vistas. |
| `GET /register` | Sin cuerpo | Renderiza el registro. |
| `GET /inicio` | Sin cuerpo | Renderiza la vista de inicio. |
| `GET /proyecto` | Sin cuerpo ni query param | Renderiza la vista `proyecto` y consulta la lista de usuarios a la API. El controlador no recibe `id_proyecto`. |
| `GET /tarea` | Sin cuerpo | Renderiza la vista `proyecto`. |
| `POST /saveUsuario` | Formulario con `nombre`, `correo_electronico`, `contrasena`, `rol`, `imagen_usuario` | Crea usuario vía `/addUsuario` y redirige a `/login`. |
| `POST /saveProyecto` | Formulario con `nombre`, `descripcion`, `fecha_inicio`, `fecha_finalizacion`, `estado_proyecto`, `fondo` | Crea proyecto vía `/addProyecto` y redirige a `/inicio`. |
| `POST /saveTarea` | Formulario con `nombre`, `descripcion`, `fecha_vencimiento`, `lista`, `estado`, `id_proyecto` | Crea tarea vía `/addTareas` y redirige a `/proyecto`. |
| `POST /saveComentario` | JSON de comentario, ver el ejemplo de API | Crea comentario vía `/addComentario`. |
| `POST /consultarProyectos` | Sin cuerpo | Devuelve la lista de proyectos vía `/buscarProyectos`. |
| `POST /consultarUsuarios` | Sin cuerpo | Devuelve la lista de usuarios vía `/buscarUsuarios`. |
| `POST /consultarTareas` | Sin cuerpo | Devuelve la lista de tareas vía `/buscarTareas`. |
| `POST /consultarComentarios` | Sin cuerpo | Devuelve la lista de comentarios vía `/buscarComentarios`. |
| `POST /verificarLogin` | JSON de login, ver `/getUsuarioLogin` en API | Reenvía las credenciales y responde con el usuario. |
| `POST /obtenerTareas` | `{"id_proyecto":1}` | Devuelve tareas del proyecto vía `/getTarea`. |
| `POST /obtenerComentarios` | `{"id_tarea":1}` | Devuelve comentarios de la tarea vía `/getComentario`. |
| `POST /actualizarProyecto` | JSON con `id_proyecto` | Busca y devuelve el proyecto; no lo actualiza. |
| `POST /actualizarTarea` | JSON con `id_tarea` | Busca y devuelve la tarea; no la actualiza. |
| `POST /eliminarProyecto` | `{"id_proyecto":1}` | Elimina vía `/deleteProyecto` y redirige a `/inicio`. |
| `POST /eliminarTarea` | `{"id_tarea":1}` | Elimina vía `/deleteTareas` y redirige a `/proyecto`. |
| `POST /verificarTarea` | JSON de tarea, ver ejemplo de API | Llama a `/addTareas` y responde el resultado numérico. |
| `POST /cambiosTareas` | JSON de tarea, ver ejemplo de API | Actualmente también llama a `/addTareas` (crea/guarda; no hace una edición especial). |

Al probar el cliente, reemplaza los IDs `1` de ejemplo por registros existentes. Para observar las respuestas JSON y probar todos los métodos HTTP de la API —incluido `DELETE`— conviene llamar directamente a `http://localhost:8081`; las rutas `eliminar...` del cliente reciben `POST` y son las que internamente envían el `DELETE`.

## Comportamientos actuales que conviene cubrir o mejorar

- Una búsqueda por ID que no existe responde actualmente con un objeto vacío (campos `null`) en vez de un `404`.
- Las altas y actualizaciones devuelven `1` si se procesa la solicitud; las comprobaciones `objeto.equals(null)` no validan de forma segura un cuerpo nulo. Prueba entradas inválidas como defectos conocidos, no como respuestas `400` garantizadas.
- Los métodos `update...` llaman a `save`; si no se envía el ID, el repositorio puede insertar un registro en vez de actualizar uno existente. Incluye una prueba para definir y corregir ese contrato.
- El cliente usa `http://localhost:8081` directamente para llamar a la API; las pruebas del front deben iniciar ambos módulos o simular esas llamadas.
- `POST /cambiosTareas` actualmente delega en `addTareas`; comprueba si debería editar por ID o crear un registro.
- Las rutas de listado (`/buscarProyectos`, `/buscarUsuarios`, `/buscarTareas`, `/buscarComentarios`, `/buscarAsignaciones`) están mapeadas como `POST`, aunque solo consultan datos. Usa ese verbo al probarlas mientras siga siendo el contrato actual.
