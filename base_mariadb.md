# Práctica 1: Pruebas de base de datos y endpoints del backend

**Valor total:** 30% de la evaluación (Parte A: 15%; Parte B: 15%).

> Esta guía sirve para planear, ejecutar y documentar las pruebas. Completa los campos entre corchetes durante la entrega. Registra resultados observados; no marques una prueba como aprobada sin ejecutarla. Las capturas y los datos de ejemplo deben corresponder a la ejecución que presentes.

## 1. Información general

| Dato | Información |
|---|---|
| Proyecto | Sistema administrador de proyectos (`admin-proyectos`) |
| Módulo / servicio backend | API REST Spring Boot (`Ejercicio5MyAPIGestor`) |
| Base de datos | MariaDB, `dbgestor` |
| Integrante(s) | [Nombres completos] |
| Rama / commit ID | [Rama] / [Commit] |
| Fecha de ejecución | [DD/MM/AAAA] |
| Herramientas | Docker Desktop, PowerShell, cliente `mariadb` del contenedor y `Invoke-RestMethod` / `Invoke-WebRequest` |

## 2. Arquitectura y alcance de las pruebas

El proyecto contiene una API REST desarrollada con Spring Boot y Java (`Ejercicio5MyAPIGestor`), una interfaz web Spring Boot (`Ejercicio5Cliente`) y una base de datos MariaDB. La API escucha en el puerto `8081`; la base de datos `dbgestor` corre en el contenedor Docker `mariadb-container` y publica el puerto `3306` en el equipo anfitrión. La API usa Spring Data JPA para persistir las entidades en MariaDB.

Las entidades principales son proyectos (`proyectos`), usuarios (`usuario`), tareas (`tareas`), comentarios (`comentarios`) y asignaciones (`asignaciones`). Las tareas se relacionan con proyectos mediante `id_proyecto`; los comentarios y asignaciones hacen referencia a tareas y usuarios. También pueden aparecer tablas de secuencia (`*_seq`, `hibernate_sequence`) generadas por JPA.

Las pruebas de esta guía cubren operaciones SQL directas y endpoints representativos de alta, consulta y manejo de una entrada inválida. El contrato actual de la API usa `POST` para algunas consultas de lista y devuelve `1` en las operaciones de escritura procesadas. En las búsquedas por ID, si no existe el registro, el controlador devuelve un objeto vacío (con campos nulos), no un `404`. Anota cualquier diferencia entre estos comportamientos actuales y el resultado observado.

## 3. Preparación del entorno

1. Abre Docker Desktop y confirma que está iniciado.
2. Desde PowerShell, en la raíz del repositorio, revisa el contenedor:

   ```powershell
   docker ps --filter "name=mariadb-container" --format "table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"
   docker inspect mariadb-container --format "Imagen: {{.Config.Image}}`nEstado: {{.State.Status}}`nPuertos: {{json .NetworkSettings.Ports}}`nMontajes: {{json .Mounts}}"
   ```

   Se espera que el contenedor esté `running` y que el puerto `3306` esté publicado. Si existe pero está detenido, puedes iniciarlo desde PowerShell con:

   ```powershell
   docker start mariadb-container
   ```

   Vuelve a comprobarlo con `docker ps`. No elimines ni recrees el contenedor o volumen con datos sin autorización.
3. Comprueba la base de datos entrando al cliente MariaDB dentro del contenedor. Copia este comando en PowerShell:

   ```powershell
   docker exec -it mariadb-container mariadb -u root -p dbgestor
   ```

   Cuando aparezca `Enter password:`, escribe la contraseña del contenedor (no se verá mientras escribes) y presiona **Enter**. No escribas la contraseña en el comando ni la incluyas en capturas. Ya dentro de MariaDB, copia y ejecuta estas consultas, una por una o todas juntas:

   ```sql
   USE dbgestor;
   SELECT DATABASE();
   SHOW TABLES;

   SELECT * FROM proyectos;
   SELECT id_usuario, nombre, rol, imagen_usuario FROM usuario;
   SELECT * FROM tareas;
   SELECT id_comentario, id_tarea, texto_comentario, fecha_hora FROM comentarios;
   SELECT * FROM asignaciones;

   SELECT COUNT(*) AS proyectos FROM proyectos;
   SELECT COUNT(*) AS usuarios FROM usuario;
   SELECT COUNT(*) AS tareas FROM tareas;
   SELECT COUNT(*) AS comentarios FROM comentarios;
   SELECT COUNT(*) AS asignaciones FROM asignaciones;
   ```

   Los nombres correctos son `proyectos` (no `PROYECTS`), `usuario`, `tareas`, `comentarios` y `asignaciones`. En `usuario` se omiten deliberadamente el correo y la contraseña; en `comentarios` se omite el identificador del usuario. Así no se muestran datos personales ni contraseñas en la salida/captura. Para salir del cliente escribe `exit;` y presiona **Enter**. Para volver a entrar más tarde repite el comando `docker exec` de arriba.
4. En una terminal, desde la raíz del repositorio, inicia la API:

   ```powershell
   Set-Location .\Ejercicio5MyAPIGestor
   .\mvnw.cmd spring-boot:run
   ```

   Déjala abierta. Abre otra terminal PowerShell para las solicitudes HTTP. La conexión de producción/desarrollo está configurada en `Ejercicio5MyAPIGestor\src\main\resources\application.properties`; verifica que sus credenciales coincidan con el MariaDB local, pero no publiques contraseñas.
5. En la terminal de pruebas, confirma que la API responde:

   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8081/buscarProyectos" -Method Post
   ```

   Debe responder con HTTP `200` y el cuerpo debe ser una lista JSON de proyectos (puede estar vacía).

## 4. Parte A: planeación y matriz documental (15%)

Usa `Content-Type: application/json` en las solicitudes con cuerpo. Los ejemplos crean registros de prueba; usa un sufijo único si repites la ejecución y conserva los IDs generados para las consultas. No actualices ni elimines datos preexistentes.

### 4.1 Matriz de pruebas

Completa **Resultado obtenido**, **Estado** y evidencia después de ejecutar cada caso. Estado: `Aprobado`, `Fallido` o `Pendiente`.

| ID | Elemento | Método / operación | Entrada / datos | Resultado esperado | Resultado obtenido | Estado / evidencia |
|---|---|---|---|---|---|---|
| P-01 | Base de datos | `SELECT` de tablas y conteos | Base `dbgestor` | Conexión exitosa; tablas principales visibles y conteos registrados | [Completar] | [Completar] |
| P-02 | Base de datos | `SELECT` del proyecto creado en P-03 | Nombre único `Practica1-[sufijo]` o ID obtenido | Proyecto persistido; la consulta devuelve el mismo ID y nombre | [Completar] | [Completar] |
| P-03 | Endpoint / proyecto | `POST /addProyecto` | JSON válido con nombre, fechas, estado y descripción | HTTP `200`; cuerpo `1`; registro recuperable en MariaDB | [Completar] | [Completar] |
| P-04 | Endpoint / proyecto | `GET /buscarProyecto?id_proyecto={id}` | ID obtenido en P-03 | HTTP `200`; respuesta JSON con el mismo ID y nombre enviado | [Completar] | [Completar] |
| P-05 | Endpoint / proyecto | `GET /buscarProyecto?id_proyecto=no-numerico` | Parámetro inválido | Error controlado; se espera HTTP `400`, sin insertar ni alterar datos | [Completar] | [Completar] |

### 4.2 Contrato de endpoints seleccionados

Base URL: `http://localhost:8081`.

| Método y ruta | Propósito / entrada | Respuesta esperada |
|---|---|---|
| `POST /addProyecto` | Crear un proyecto. Cuerpo JSON: `nombre`, `descripcion`, `fecha_inicio`, `fecha_finalizacion`, `estado_proyecto`, `fondo`. | HTTP `200`, cuerpo numérico `1` cuando se guarda correctamente. |
| `GET /buscarProyecto?id_proyecto={id}` | Consultar por el ID devuelto al crear el proyecto; no lleva cuerpo. | HTTP `200`, objeto JSON del proyecto. |
| `POST /buscarProyectos` | Listar proyectos; no lleva cuerpo. | HTTP `200`, arreglo JSON de proyectos. |
| `GET /buscarProyecto?id_proyecto=no-numerico` | Comprobar el manejo de un parámetro que no puede convertirse a entero. | Se espera HTTP `400` de Spring; registra el resultado real y anota cualquier diferencia. |

**Herramienta de endpoints:** PowerShell `Invoke-RestMethod` para respuestas JSON y `Invoke-WebRequest` para registrar el código HTTP. **Verificación de persistencia:** consulta SQL en el cliente MariaDB ejecutado dentro del contenedor Docker.

### 4.3 Peticiones completas listas para copiar

Abre **dos ventanas de PowerShell**: una para dejar la API iniciada y otra para las pruebas. En la ventana de pruebas copia el bloque siguiente completo. Genera un nombre único, envía el cuerpo JSON de alta, imprime el código HTTP y la respuesta, y guarda el nombre para buscar la fila en MariaDB:

```powershell
$api = "http://localhost:8081"
$sufijo = Get-Date -Format "yyyyMMddHHmmss"
$nombreProyecto = "Practica1-$sufijo"

# P-03: cuerpo JSON completo para POST /addProyecto
$cuerpoProyecto = @{
  nombre = $nombreProyecto
  descripcion = "Registro temporal para la Práctica 1"
  fecha_inicio = "2026-10-01"
  fecha_finalizacion = "2026-12-01"
  estado_proyecto = "Activo"
  fondo = ""
} | ConvertTo-Json

# Petición: POST http://localhost:8081/addProyecto
$respuestaAlta = Invoke-WebRequest `
  -Uri "$api/addProyecto" `
  -Method Post `
  -ContentType "application/json" `
  -Body $cuerpoProyecto

"HTTP: $([int]$respuestaAlta.StatusCode)"
"Respuesta: $($respuestaAlta.Content)"
"Nombre para buscar en MariaDB: $nombreProyecto"
```

El cuerpo JSON enviado queda con esta forma (el valor de `nombre` cambia automáticamente con el sufijo):

```json
{
  "nombre": "Practica1-20261007193000",
  "descripcion": "Registro temporal para la Práctica 1",
  "fecha_inicio": "2026-10-01",
  "fecha_finalizacion": "2026-12-01",
  "estado_proyecto": "Activo",
  "fondo": ""
}
```

El resultado esperado de P-03 es HTTP `200` y cuerpo `1`. A continuación ejecuta P-02 (consulta SQL) desde PowerShell. El comando pedirá la contraseña de MariaDB sin mostrarla:

```powershell
docker exec -it mariadb-container mariadb -u root -p dbgestor `
  -e "SELECT * FROM proyectos WHERE nombre = '$nombreProyecto';"
```

La consulta debe mostrar la fila insertada y el `id_proyecto` generado. Para consultar manualmente en el cliente interactivo, entra con `docker exec -it mariadb-container mariadb -u root -p dbgestor` y pega:

```sql
SELECT * FROM proyectos WHERE nombre = 'Practica1-20261007193000';
```

En esa consulta reemplaza el ejemplo por el nombre exacto que imprimió PowerShell. Copia el `id_proyecto` que aparece; en la ventana de pruebas asígnalo a la variable `$idProyecto` para las siguientes peticiones:

```powershell
$idProyecto = [int](Read-Host "Escribe el id_proyecto que mostró MariaDB")
```

**P-04: consultar el proyecto por ID**. Esta petición GET no lleva cuerpo JSON; el ID va en la URL:

```powershell
# Petición: GET http://localhost:8081/buscarProyecto?id_proyecto=<ID>
$respuestaConsulta = Invoke-WebRequest `
  -Uri "$api/buscarProyecto?id_proyecto=$idProyecto" `
  -Method Get

"HTTP: $([int]$respuestaConsulta.StatusCode)"
"Respuesta JSON: $($respuestaConsulta.Content)"
```

Se espera HTTP `200` y un objeto JSON que contenga el mismo `id_proyecto` y nombre obtenido en P-02.

**P-05: enviar un parámetro inválido**. Tampoco lleva cuerpo; el texto inválido se envía en el parámetro de la URL:

```powershell
# Petición: GET http://localhost:8081/buscarProyecto?id_proyecto=no-numerico
try {
  $respuestaInvalida = Invoke-WebRequest `
    -Uri "$api/buscarProyecto?id_proyecto=no-numerico" `
    -Method Get

  "HTTP inesperado: $([int]$respuestaInvalida.StatusCode)"
  "Respuesta: $($respuestaInvalida.Content)"
} catch {
  if ($_.Exception.Response) {
    "HTTP recibido: $([int]$_.Exception.Response.StatusCode)"
  }
  "Detalle: $($_.ErrorDetails.Message)"
}
```

Se espera HTTP `400` por el parámetro que no se puede convertir a entero. Registra el código y detalle que realmente aparezcan; el bloque captura el error para que PowerShell pueda mostrar la evidencia sin interrumpir la sesión.

**Consulta de lista adicional**, correspondiente a `POST /buscarProyectos`; no envía cuerpo:

```powershell
$respuestaLista = Invoke-WebRequest `
  -Uri "$api/buscarProyectos" `
  -Method Post
"HTTP: $([int]$respuestaLista.StatusCode)"
"Proyectos JSON: $($respuestaLista.Content)"
```

## 5. Parte B: ejecución y evidencias de base de datos (15%)

### 5.1 P-01 — Conexión y estado de la base

Ejecuta las consultas de conexión, estructura, registros completos de las tablas y conteos. Así se muestran las filas existentes además del total de cada tabla:

```sql
USE dbgestor;
SELECT DATABASE();
SHOW TABLES;

SELECT * FROM proyectos;
SELECT id_usuario, nombre, rol, imagen_usuario FROM usuario;
SELECT * FROM tareas;
SELECT id_comentario, id_tarea, texto_comentario, fecha_hora FROM comentarios;
SELECT * FROM asignaciones;

SELECT COUNT(*) AS proyectos FROM proyectos;
SELECT COUNT(*) AS usuarios FROM usuario;
SELECT COUNT(*) AS tareas FROM tareas;
SELECT COUNT(*) AS comentarios FROM comentarios;
SELECT COUNT(*) AS asignaciones FROM asignaciones;
```

Registra los resultados. En `usuario` se muestran solo las columnas indicadas para no exponer correos ni contraseñas; en `comentarios` se omite `id_usuario`. Esta sección solicita evidencias **únicamente de MariaDB y Docker**; las capturas de endpoints del backend no son necesarias aquí porque ya terminaste esa parte.

### 5.2 P-02 — Comprobar persistencia desde MariaDB

Después de crear el proyecto de prueba siguiendo el bloque de P-03 en la sección 4.3, ejecuta el `SELECT` de P-02 desde MariaDB. Verifica que la fila exista y anota su ID y nombre. No es necesario volver a capturar las pruebas de backend P-03, P-04 y P-05 para este apartado.

### 5.3 Evidencias: qué capturas tomar

Toma y guarda **una evidencia independiente por cada ID desglosado**. Estas capturas son solo de Docker y MariaDB; no necesitas volver a capturar endpoints del backend.

| ID de evidencia | Nombre de archivo | Qué debe aparecer |
|---|---|---|
| P-01A | `P-01A-docker-contenedor.png` | Docker Desktop con `mariadb-container` en estado `Running`, imagen MariaDB y puerto publicado `3306`. |
| P-01B | `P-01B-conexion-dbgestor.png` | Terminal conectada al cliente MariaDB; mostrar `SELECT DATABASE();` y el resultado `dbgestor`. No debe verse la contraseña. |
| P-01C | `P-01C-tablas.png` | Comando `SHOW TABLES;` y resultado con las tablas de la base. |
| P-01D | `P-01D-conteos.png` | Consultas `COUNT(*)` y conteos resultantes para `proyectos`, `usuario`, `tareas`, `comentarios` y `asignaciones`. Si no caben legiblemente, divide esta evidencia en `P-01D1` y `P-01D2`. |
| P-01E | `P-01E-registros-base.png` | Una o más consultas de lectura de las tablas: `proyectos`, `tareas` y `asignaciones`; de `usuario` y `comentarios` solo las columnas no sensibles indicadas en la sección 3. |
| P-02 | `P-02-proyecto-persistido.png` | Consulta `SELECT` filtrada por el nombre único del proyecto de prueba, con la fila y su `id_proyecto` visibles. |

**Cómo capturar cada ID:**

1. Ejecuta en MariaDB únicamente el comando asociado al ID que vas a evidenciar y deja visibles tanto el comando como su resultado.
2. Presiona **Win + Shift + S**, selecciona el área de la pantalla y guarda el PNG con el nombre indicado en la tabla.
3. Repite el proceso por cada ID. Si el resultado es muy largo, toma varias capturas legibles agregando `-1`, `-2` al nombre (por ejemplo, `P-01E-registros-base-1.png`).
4. Guarda todos los archivos en `evidencias\practica1`. Comprueba que no aparezcan contraseñas, correos, contraseñas de usuario ni otros datos personales. No captures `SELECT * FROM usuario`; usa la consulta de columnas seguras indicada en la sección 3.

Como evidencia complementaria de Docker, puedes guardar la salida de estos comandos en PowerShell (no sustituyen las capturas de los resultados SQL):

```powershell
docker ps --filter "name=mariadb-container"
docker logs --tail 50 mariadb-container
```

### 5.4 Registro de fallas de base de datos y correcciones

Si no hubo fallas en la conexión o en las consultas de base de datos, escribe `Sin fallas de base de datos detectadas`. Si hubo fallas, completa una fila por cada una e incluye la evidencia previa y posterior a la corrección.

| ID del caso | Falla detectada | Causa identificada | Corrección realizada | Reprueba | Resultado final |
|---|---|---|---|---|---|
| [ID o N/A] | [Descripción] | [Causa] | [Cambio aplicado] | [Fecha y evidencia] | [Aprobado / Pendiente] |

## 6. Evidencias que se entregan

- Capturas de Docker y MariaDB indicadas en la sección 5.3, identificadas como P-01 o P-02.
- Resultados SQL de conexión, tablas, conteos y consulta del proyecto de prueba.
- Matriz con los resultados obtenidos en las pruebas de base de datos y su evidencia correspondiente.
- Registro de fallas de base de datos y correcciones, incluida la reprueba, si aplica.

## 7. Criterios de evaluación

| Criterio | Valor | Evidencia |
|---|---:|---|
| Documentación de pruebas: casos, entradas y resultados esperados | 6% | Parte A |
| Diseño técnico: relación entre base de datos, operaciones y endpoints | 5% | Parte A |
| Claridad y organización | 4% | Parte A |
| Pruebas de base de datos y persistencia/consulta | 5% | Parte B |
| Pruebas de endpoints, solicitudes, códigos y respuestas | 6% | Parte B |
| Corrección y reprueba de fallas | 4% | Parte B |
| **Total** | **30%** | |

## 8. Resultado general

- Parte A — Teoría / documentación: ______ / 15%
- Parte B — Evidencia de ejecución: ______ / 15%
- Calificación total: ______ / 30%
- Resultado: [ ] APROBADO  [ ] REQUIERE CORRECCIÓN
