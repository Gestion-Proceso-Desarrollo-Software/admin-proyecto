# Pruebas manuales de la base de datos

Esta guía permite comprobar que los datos creados desde la aplicación o la API se guarden y se consulten correctamente en MariaDB. Ejecuta las pruebas en un entorno local y usa exclusivamente registros de prueba.

## 1. Requisitos y preparación

- Docker Desktop iniciado y el contenedor `mariadb-container` en ejecución.
- La base `dbgestor` disponible en MariaDB.
- La API y, para los casos que se originan en la interfaz, la aplicación web encendidas.
- Registros de prueba identificables con un sufijo único, por ejemplo, fecha y hora. No uses datos ni contraseñas personales.

Desde PowerShell, en la raíz del repositorio, abre el cliente MariaDB:

```powershell
docker exec -it mariadb-container mariadb -u root -p dbgestor
```

Escribe la contraseña cuando el cliente la solicite; no la incluyas en el comando ni en capturas. Ejecuta las consultas de esta guía dentro del cliente. Para salir, escribe `exit;`.

Completa los valores entre corchetes con los datos de prueba usados en la interfaz o en la API. En SQL, reemplaza también los corchetes y conserva las comillas simples. Para ejecutar las altas, actualizaciones y eliminaciones de la aplicación/API, consulta los flujos de [`PRUEBAS.md`](PRUEBAS.md); esta guía se centra en confirmar el resultado directamente en MariaDB.

## 2. Criterios generales

1. No actualices ni elimines registros preexistentes. Crea datos únicos para cada ejecución.
2. Verifica las altas, cambios y eliminaciones con consultas `SELECT`.
3. En las consultas de usuarios, no selecciones ni captures `contrasena` ni el correo de otros usuarios. Filtra por el correo ficticio de prueba y muestra únicamente las columnas indicadas.
4. En cada caso registra **Aprobado**, **Fallido** o **No ejecutado**, junto con el resultado observado.
5. Las relaciones entre tareas, comentarios, usuarios y proyectos se comprueban por sus identificadores. El esquema actual no declara claves foráneas para todas esas relaciones, así que no se debe dar por garantizado el borrado en cascada.

## 3. Pruebas manuales

### BD-01 — Conexión y estructura de la base

1. Conéctate al cliente MariaDB según la sección 1.
2. Ejecuta:

   ```sql
   SELECT DATABASE();
   SHOW TABLES;
   ```

3. Confirma que la base activa sea `dbgestor` y que estén las tablas `proyectos`, `usuario`, `tareas`, `comentarios` y `asignaciones`.

**Resultado esperado:** la conexión se establece y aparecen las tablas principales de la aplicación. También pueden existir tablas de secuencia creadas por JPA.

### BD-02 — Conteo inicial sin exponer datos sensibles

1. Ejecuta las consultas de conteo:

   ```sql
   SELECT COUNT(*) AS proyectos FROM proyectos;
   SELECT COUNT(*) AS usuarios FROM usuario;
   SELECT COUNT(*) AS tareas FROM tareas;
   SELECT COUNT(*) AS comentarios FROM comentarios;
   SELECT COUNT(*) AS asignaciones FROM asignaciones;
   ```

2. Anota los resultados como referencia antes de crear los registros de prueba. No ejecutes `SELECT * FROM usuario`.

**Resultado esperado:** cada consulta devuelve un conteo y no se muestran contraseñas ni datos de usuarios ajenos a la prueba.

### BD-03 — Persistencia de un usuario registrado

1. Registra desde `/register` un usuario de prueba con un correo único, siguiendo el caso F-04 de [`Pruebas-front.md`](Pruebas-front.md).
2. En MariaDB, consulta únicamente sus columnas no sensibles:

   ```sql
   SELECT id_usuario, nombre, rol, imagen_usuario
   FROM usuario
   WHERE correo_electronico = '[correo-de-prueba]';
   ```

3. Cierra el cliente MariaDB, vuelve a conectarte y repite la consulta para confirmar que el registro continúa guardado.
4. Inicia sesión con el usuario de prueba y verifica que la aplicación lo reconoce.

**Resultado esperado:** se encuentra una sola fila con el nombre y rol de prueba. El registro sigue disponible después de reconectar y permite iniciar sesión. No consultes ni captures la contraseña.

### BD-04 — Persistencia de un proyecto y sus cambios

1. Crea desde la aplicación un proyecto con nombre único, siguiendo F-06 de [`Pruebas-front.md`](Pruebas-front.md).
2. Consulta la fila por su nombre:

   ```sql
   SELECT id_proyecto, nombre, descripcion, fecha_inicio,
          fecha_finalizacion, estado_proyecto, fondo
   FROM proyectos
   WHERE nombre = '[nombre-unico-del-proyecto]';
   ```

3. Anota el `id_proyecto`. Edita el proyecto desde la aplicación y vuelve a ejecutar la consulta.

**Resultado esperado:** la fila existe con los datos enviados; después de editarla, los campos modificados muestran los nuevos valores y el ID permanece igual.

### BD-05 — Persistencia de tareas asociadas a un proyecto

1. Crea una tarea de prueba en el proyecto de BD-04, siguiendo F-07 de [`Pruebas-front.md`](Pruebas-front.md).
2. Consulta la tarea junto con el nombre del proyecto asociado:

   ```sql
   SELECT t.id_tarea, t.nombre, t.lista, t.id_proyecto,
          p.nombre AS proyecto
   FROM tareas AS t
   LEFT JOIN proyectos AS p ON p.id_proyecto = t.id_proyecto
   WHERE t.nombre = '[nombre-unico-de-la-tarea]';
   ```

3. Comprueba que `id_proyecto` coincida con el anotado en BD-04 y que el proyecto asociado sea el esperado.
4. Si modificaste la tarea siguiendo F-08, vuelve a consultarla y compara los campos cambiados.

**Resultado esperado:** la tarea queda guardada una sola vez, asociada al ID del proyecto de prueba; los cambios realizados se reflejan en la consulta.

### BD-06 — Persistencia de comentarios de tarea

1. Agrega un comentario a la tarea de prueba siguiendo F-09 de [`Pruebas-front.md`](Pruebas-front.md).
2. Consulta los comentarios filtrando por el `id_tarea` anotado en BD-05:

   ```sql
   SELECT id_comentario, id_tarea, texto_comentario, fecha_hora
   FROM comentarios
   WHERE id_tarea = [id-tarea-de-prueba];
   ```

3. Comprueba que el texto y la fecha correspondan al comentario enviado.

**Resultado esperado:** el comentario existe y su `id_tarea` corresponde a la tarea de prueba. Si no aparece, registra el error observado y revisa la fecha enviada por la aplicación.

### BD-07 — Persistencia de asignaciones

1. Usa un usuario y una tarea de prueba existentes. Crea una asignación con `POST /addAsignacion`, siguiendo el ejemplo de asignaciones en [`PRUEBAS.md`](PRUEBAS.md).
2. Consulta la asignación filtrando por el ID de la tarea:

   ```sql
   SELECT id_asignacion, id_usuario, id_tarea, fecha_asignacion
   FROM asignaciones
   WHERE id_tarea = [id-tarea-de-prueba]
     AND id_usuario = [id-usuario-de-prueba];
   ```

3. Comprueba que los dos identificadores sean los usados al crear la asignación.

**Resultado esperado:** se encuentra la asignación con el usuario y la tarea de prueba correctos y la fecha enviada.

### BD-08 — Eliminación y limpieza de registros de prueba

1. Elimina desde la aplicación/API únicamente el proyecto y la tarea de prueba, siguiendo F-10 y F-12 de [`Pruebas-front.md`](Pruebas-front.md). Elimina también los comentarios y asignaciones creados para estas pruebas, si aplica.
2. Repite las consultas de BD-04 a BD-07 usando los mismos identificadores o valores únicos.
3. Si eliminaste el usuario de prueba, verifica su ausencia sin consultar la contraseña:

   ```sql
   SELECT COUNT(*) AS usuarios_de_prueba
   FROM usuario
   WHERE correo_electronico = '[correo-de-prueba]';
   ```

**Resultado esperado:** las consultas ya no devuelven los registros eliminados y el conteo del usuario de prueba es `0`. Los registros ajenos a la prueba permanecen sin cambios.

## 4. Registro de resultados

| ID | Caso | Resultado (Aprobado/Fallido/No ejecutado) | Evidencia o comentario |
|---|---|---|---|
| BD-01 | Conexión y estructura | | |
| BD-02 | Conteos iniciales | | |
| BD-03 | Persistencia de usuario | | |
| BD-04 | Persistencia y actualización de proyecto | | |
| BD-05 | Tarea asociada a proyecto | | |
| BD-06 | Comentario asociado a tarea | | |
| BD-07 | Persistencia de asignación | | |
| BD-08 | Eliminación y limpieza | | |

