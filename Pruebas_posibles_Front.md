# Pruebas posibles de Frontend (`Pruebas_posibles_Front`)

Considerando que las fallas del backend (`Ejercicio5MyAPIGestor`) y de la base de datos (`dbgestor` en MariaDB) ya fueron corregidas —incluyendo respuestas HTTP semánticas (`400 Bad Request`, `401 Unauthorized`, `404 Not Found`), validaciones de campos obligatorios (`@Valid`, `@NotBlank`, `@Email`), ocultamiento y cifrado de contraseñas (`BCrypt`), y restricciones de integridad referencial (`FOREIGN KEY` y `UNIQUE`)—, se identifican los siguientes **bugs actuales y potenciales en el frontend (`Ejercicio5Cliente`)**, junto con el procedimiento paso a paso para reproducirlos y las evidencias que se deben capturar.

---

## 0. Preparación del entorno antes de iniciar

1. **Verifica que MariaDB esté corriendo en Docker:**
   ```powershell
   docker ps --filter "name=mariadb-container"
   ```
2. **Inicia el Backend (`8081`) en una terminal PowerShell:**
   ```powershell
   Set-Location ".\Ejercicio5MyAPIGestor"
   .\mvnw.cmd spring-boot:run
   ```
3. **Inicia el Frontend (`8080`) en una segunda terminal PowerShell:**
   ```powershell
   Set-Location ".\Ejercicio5MyAPIGestor"
   .\mvnw.cmd -f ..\Ejercicio5Cliente\pom.xml spring-boot:run
   ```
4. **Prepara el navegador para tomar evidencias:**
   - Abre `http://localhost:8080/` en Chrome o Edge.
   - Presiona **`F12`** (Herramientas de desarrollador) y acomoda el panel a la derecha o abajo para que en **una misma captura** se vea la **interfaz web** y la pestaña **Console** o **Network**.
   - Usa **`Win + Shift + S`** para recortar cada evidencia.

---

## 1. Bugs de integración en Frontend tras corregir Backend y Base de Datos

| ID | Módulo / Archivo | Bug / Falla potencial en Frontend | Causa en el código del Frontend | Resultado esperado tras corregir Front |
|---|---|---|---|---|
| **FE-INT-01** | Login (`login.js`, `GestorController.java`) | Al ingresar contraseña incorrecta o correo inexistente, la pantalla no muestra el toast `#alerta5` («El usuario o contraseña no existen») y la petición falla con error `500` en consola. | `login.js` solo evalúa `if (data.nombre == '000')` dentro de `success:` y carece de bloque `error:`. Al devolver el backend `HTTP 401 Unauthorized` (corrección de NEG-08 y NEG-09), `RestTemplate.postForEntity()` en `GestorController.verificarUsuario()` lanza `HttpClientErrorException`. | `GestorController` y `login.js` deben capturar el estado `401` y mostrar el toast `#alerta5` sin lanzar error `500`. |
| **FE-INT-02** | Registro (`register.html`, `GestorController.java`) | Al registrar un correo ya existente o rechazado por `@Email` en el backend, el navegador muestra una pantalla blanca de error (*Whitelabel Error Page* `500`) en vez de un mensaje en el formulario. | `GestorController.setSaveUsuario()` invoca `postForEntity()` hacia `/addUsuario` sin bloque `try/catch` para códigos `400 Bad Request` o `409 Conflict` (corrección de NEG-10 y `UNIQUE` en BD), redirigiendo siempre a `/login` solo si no hay excepción. | El controlador web debe capturar el rechazo del backend y devolver a `/register` mostrando el mensaje de error correspondiente. |
| **FE-INT-03** | Tablero / Alta rápida de tareas (`proyecto.js`, `GestorController.java`) | El botón «guardar» en `+ Añadir tarea` (en cualquiera de las 3 listas) falla por validación del backend o crea tarjetas sin prioridad ni fecha. | `validarTarea()`, `validarTarea2()` y `validarTarea3()` en `proyecto.js` envían únicamente `{nombre, lista, id_proyecto}`, omitiendo `descripcion`, `fecha_vencimiento` y `estado`, además de permitir enviar `nombre` vacío. Con las validaciones de entidad activas en el backend, la API rechaza el alta con `HTTP 400`. | Validar en JS que el nombre no esté vacío y asignar valores por defecto válidos (`estado: "primary"`, descripción/fecha por defecto) o manejar el `400`. |
| **FE-INT-04** | Edición de Proyectos y Tareas (`inicio.html`, `proyecto.html`, `GestorController.java`) | Al editar un proyecto o una tarea existente, el frontend invoca los endpoints de **creación** (`/addProyecto`, `/addTareas`) en lugar de los de **actualización** (`/updateProyecto`, `/updateTareas`). | En `inicio.html` el modal `#modalEditar` apunta a `th:action="saveProyecto"` (`/addProyecto`), y en `proyecto.html` el modal `#editarTarea` apunta a `th:action="saveTarea"` (`/addTareas`); incluso `/cambiosTareas` en `GestorController` redirige a `/addTareas`. | Los formularios de edición deben invocar rutas dedicadas que consuman `/updateProyecto` y `/updateTareas` en la API. |
| **FE-INT-05** | Comentarios (`proyecto.js`, `GestorController.java`) | El alta de comentarios falla o devuelve error de deserialización/integridad referencial al enviar el nombre del usuario en vez de su ID y una fecha con año de 2 dígitos. | En `proyecto.js`, `guardarComentario()` envía `id_usuario: usuario.nombre`, `id_tarea` como `String` y `fecha_hora` con formato `YY/MM/DD HH:mm`. Además, `GestorController.setSaveComentario()` devuelve el nombre de vista `"proyecto"` sin `@ResponseBody`. | Enviar `id_usuario` válido según el esquema corregido, `id_tarea` numérico, fecha en formato `YYYY-MM-DD HH:mm` y responder JSON/código HTTP en `setSaveComentario()`. |
| **FE-INT-06** | Eliminación de Proyectos y Tareas (`inicio.js`, `proyecto.js`, `GestorController.java`) | Al intentar eliminar un proyecto que tiene tareas asociadas (o una tarea con comentarios/asignaciones), la eliminación falla en el backend por `FOREIGN KEY`, pero el frontend no informa al usuario. | Con las claves foráneas activas en MariaDB (corrección de NEG-13, NEG-18 y NEG-20), MariaDB impide borrar registros padre con hijos activos si no hay `ON DELETE CASCADE`. `eliminarProyecto()` y `eliminarTarea()` no tienen callback `error:` en `$.ajax`. | Mostrar una alerta o toast indicando que el proyecto/tarea no puede eliminarse mientras tenga registros dependientes (o confirmar el borrado en cascada). |
| **FE-INT-07** | Perfil de usuario en Tablero (`proyecto.js`, `login.js`) | Posible pérdida de datos o fallo al leer propiedades del usuario en `localStorage` tras ocultar `contrasena` en el backend (corrección de H-01 y NEG-07). | Si existe un objeto antiguo o nulo en `localStorage.getItem("usuario")`, `proyecto.js` intenta leer `usuario.imagen_usuario` y `usuario.nombre[0]` sin verificar que el objeto o sus propiedades existan. | Si `localStorage.getItem("usuario")` es `null`, redirigir automáticamente a `/login` antes de renderizar el tablero. |

---

## 2. Bugs propios de la lógica, interfaz y validación del Frontend

| ID | Módulo / Archivo | Bug detectado en el código del Frontend | Causa técnica exacta | Resultado esperado tras corregir Front |
|---|---|---|---|---|
| **FE-UI-01** | Edición de Proyecto (`inicio.js`, `inicio.html`) | Error en tiempo de ejecución al pulsar «Guardar» en el modal de Editar proyecto: `TypeError: Cannot read properties of null (reading 'value')`. | En `inicio.html` (línea 110) el campo oculto tiene `id="editar_fondo"`, pero en `inicio.js` (líneas 129–131) la función `asignarFondoEditar()` busca `document.getElementById("editarFondo")` (sin guion bajo). | Unificar el identificador a `editar_fondo` tanto en `inicio.html` como en `asignarFondoEditar()` dentro de `inicio.js`. |
| **FE-UI-02** | Selección de Fondo (`inicio.html`) | Nueve colores sólidos de fondo generan imágenes rotas (`404 Not Found`) en las tarjetas de proyectos creados o editados. | En `inicio.html` (líneas 152–165), los eventos `onclick="guardarRutaImagen(...)"` de `azulCobalto`, `azulReal`, `azulTurquesa`, `morado`, `naranja` (2.ª fila), `negro`, `rojoBrillante`, `rosa` y `turquesaOscuro` omiten la extensión `.png`. | Todas las llamadas a `guardarRutaImagen()` deben incluir la extensión `.png` completa (p. ej. `'/Images/azulCobalto.png'`). |
| **FE-UI-03** | Selección de Fondo en Edición (`inicio.js`, `inicio.html`) | Duplicidad de la función `guardarRutaImagen()`: la versión de `inicio.js` solo actualiza `#fondo` y no actualiza `#editar_fondo` ni cierra el diálogo. | `guardarRutaImagen` está declarada tanto en `inicio.js` (línea 61) como en un `<script>` al final de `inicio.html` (línea 254), generando colisión en el ámbito global. | Eliminar la función duplicada y mantener una sola implementación en `inicio.js` que actualice `#fondo`, `#editar_fondo` y cierre `#imagenesFondo`. |
| **FE-UI-04** | Formularios (`register.html`, `inicio.html`, `proyecto.html`) | Los `<select>` permiten enviar la opción por defecto «Seleccione una opción» / «Cambiar de lista» porque su `value="0"` es considerado válido por HTML5 `required`. | En `register.html` (`rol`), `inicio.html` (`estado_proyecto`) y `proyecto.html` (`estado` y `lista`), la opción inicial tiene `value="0"` en lugar de `value=""`. Si se guarda una tarea con `lista="0"`, desaparece del tablero porque solo se grafican `"pendientes"`, `"encurso"` y `"terminado"`. | Cambiar las opciones iniciales de todos los `<select>` a `<option value="" selected disabled>Seleccione una opción</option>`. |
| **FE-UI-05** | Tablero de Tareas (`proyecto.js`) | Las tareas se duplican visualmente en las columnas del tablero cuando existen tareas en distintas listas (`pendientes`, `encurso`, `terminado`). | En `proyecto.js` (líneas 67–216), `tarjeta`, `tarjeta2`, `tarjeta3`, `lateral1`, `lateral2` y `lateral3` se declaran con `var` sin limpiarse en cada iteración, y los `.append(...)` (líneas 208–214) se ejecutan **fuera** de los `if` en cada vuelta del `data.forEach`. | Mover cada `contenedor.append(...)` y `contenedorLateral.append(...)` dentro de su respectivo bloque `if (tarea.lista == ...)`. |
| **FE-UI-06** | Eliminación de Tareas (`proyecto.js`) | Las tareas en las columnas «En curso» y «Trabajos terminados» se eliminan inmediatamente al hacer clic en el icono de basura, sin mostrar el modal de confirmación. | En `proyecto.js`, las tareas de `"pendientes"` abren `#modalAlerta` con `obtenerInfo(...)` (línea 74), pero en `"encurso"` (línea 122) y `"terminado"` (línea 167) el botón invoca directamente `onclick="eliminarTarea(${tarea.id_tarea})"`. | Usar `data-bs-toggle="modal" data-bs-target="#modalAlerta" onclick="obtenerInfo(...)"` en las tres columnas de tareas. |
| **FE-UI-07** | Redirección al guardar tarea desde modal (`GestorController.java`, `proyecto.js`) | Al guardar cambios en el modal `#editarTarea`, la redirección `redirect:/proyecto` puede perder el contexto o fallar si `id_proyecto` viene vacío. | En `proyecto.html` el formulario envía a `POST /saveTarea` (`GestorController.setSaveTareas`), mientras que la carga inicial de `/proyecto` depende del query param `?id_proyecto=` leído por `proyecto.js`. | Garantizar que `#id_proyecto` siempre se envíe poblado en el formulario del modal y que la redirección conserve `?id_proyecto={id}`. |
| **FE-UI-08** | Página Principal (`home.html`) | Error JavaScript en consola al abrir la página de inicio `/`: `Uncaught TypeError: Cannot read properties of null (reading 'addEventListener')`. | En `home.html` (líneas 226 y 234), el script intenta agregar `addEventListener` a `#registro` y `#form-registro`, elementos que están comentados en el HTML (líneas 115 y 169). | Eliminar o verificar con `if (elemento)` los listeners de `#registro` y `#form-registro` en `home.html`. |
| **FE-UI-09** | Modales sin funcionalidad y código residual (`proyecto.html`, `login.js`) | Existen modales y botones rotos o con referencias a otro sistema (`eliminarPrestamo`), además de foco incorrecto al faltar contraseña en login. | En `proyecto.html` (línea 368) el modal `#eliminarTarea` invoca `eliminarPrestamo(${prestamo.id})`; los modales `#tresPuntitosTP/EC/TT` no tienen acción en «Guardar cambios»; y en `login.js` (línea 24), cuando falta la contraseña, el `.focus()` se envía al campo de email (`#exampleInputEmail1`) en vez de `#exampleInputPassword1`. | Corregir `focus()` hacia `#exampleInputPassword1` en `login.js` y depurar los modales inactivos o residuales en `proyecto.html`. |
| **FE-UI-10** | Seguridad XSS / Comillas en nombres (`inicio.js`, `proyecto.js`) | Si el nombre de un proyecto o tarea contiene comillas simples (`'`) o etiquetas HTML, se rompe el atributo `onclick` o se inyecta HTML en el DOM. | Las tarjetas se construyen concatenando template literals sin escapar (`onclick="obtenerInfo(${id}, 'hiddenId', '${proyecto.nombre}')"` y `<h5 class="card-title">${proyecto.nombre}</h5>`). | Escapar caracteres especiales o asignar eventos y textos mediante `.text()` / `dataset` en lugar de interpolar cadenas en `onclick`. |

---

## 3. Guía paso a paso de ejecución y qué evidencia tomar

### Paso 1 — Página principal (`/`): Error JS por elementos comentados (`FE-UI-08`)
1. Abre `http://localhost:8080/` y presiona **`F12` → pestaña Console**.
2. Recarga con `F5`.
3. **Qué observar:** Aparece en rojo `Uncaught TypeError: Cannot read properties of null (reading 'addEventListener')` apuntando a `home.html:227`.
4. **📸 Evidencia (`FE-UI-08-home-console-error.png`):** Captura de la pantalla de inicio junto con la pestaña **Console** mostrando el `TypeError`.

---

### Paso 2 — Login (`/login`): Foco incorrecto y fallo de manejo de `401` (`FE-UI-09`, `FE-INT-01`)
1. Haz clic en **Ingresar** (`http://localhost:8080/login`).
2. **Prueba de foco (`FE-UI-09`):** Escribe un correo válido (`ana22533@cbtis75.edu.mx`), deja la contraseña **vacía** y pulsa **Iniciar sesión**.
   - **Qué observar:** Aparece el toast «Ingrese una contraseña», pero el borde azul de foco (`.focus()`) se coloca en el campo **Email** en vez del campo **Contraseña**.
   - **📸 Evidencia (`FE-UI-09-login-foco-incorrecto.png`):** Captura mostrando el toast «Ingrese una contraseña» y el cursor/borde activo sobre el input de Email.
3. **Prueba de rechazo `401` (`FE-INT-01`):** Escribe un correo inexistente o contraseña incorrecta, abre **`F12` → Network** y pulsa **Iniciar sesión**.
   - **Qué observar:** Con el backend corregido devolviendo `401`, `/verificarLogin` falla en `Network` (o devuelve error) y el toast `#alerta5` no se dispara porque `login.js` no tiene bloque `error:`.
   - **📸 Evidencia (`FE-INT-01-login-error-handling.png`):** Captura de `/login` con la pestaña **Network / Console** mostrando la respuesta de `/verificarLogin`.

---

### Paso 3 — Registro (`/register`): Rol `"0"` válido y correo duplicado/inválido (`FE-UI-04`, `FE-INT-02`)
1. Entra a `http://localhost:8080/register`.
2. **Prueba de `<select>` con valor `"0"` (`FE-UI-04`):** Llena Nombre, Email y Contraseña, pero deja el campo **Rol** en `Seleccione una opción` (`value="0"`) y pulsa **Registrar**.
   - **Qué observar:** El formulario no marca error en Rol (aparece en verde con palomita de Bootstrap) y envía `rol = "0"`.
   - **📸 Evidencia (`FE-UI-04-select-valor-cero.png`):** Captura del formulario validado o de la petición en **Network → Payload** mostrando `rol: 0`.
3. **Prueba de rechazo del backend en registro (`FE-INT-02`):** Intenta registrar el mismo correo por segunda vez o un correo sin TLD (`usuario@correo`).
   - **Qué observar:** Cuando el backend responde `400`/`409`, `GestorController.setSaveUsuario()` no captura la excepción y muestra la página *Whitelabel Error Page* (`HTTP 500`).
   - **📸 Evidencia (`FE-INT-02-registro-whitelabel.png`):** Captura de la pantalla de error o respuesta en **Network** al enviar `/saveUsuario`.

---

### Paso 4 — Proyectos (`/inicio`): Imágenes sin `.png`, error en `editarFondo` y endpoint de edición (`FE-UI-02`, `FE-UI-01`, `FE-UI-03`, `FE-INT-04`)
1. Inicia sesión con un usuario válido para entrar a `http://localhost:8080/inicio`.
2. **Prueba de fondos sin extensión `.png` (`FE-UI-02`):**
   - Pulsa **Agregar**, llena los datos del proyecto y haz clic en **Elegir un color de fondo**.
   - Selecciona el 2.º color de la segunda fila de Sólidos (**Azul Cobalto** o **Morado**), el cual invoca `guardarRutaImagen('/Images/azulCobalto')` sin `.png`, y guarda el proyecto.
   - **Qué observar:** La tarjeta creada aparece con el icono de imagen rota y en **F12 → Network / Console** aparece `GET http://localhost:8080/Images/azulCobalto 404 (Not Found)`.
   - **📸 Evidencia (`FE-UI-02-fondo-sin-extension-404.png`):** Captura de la tarjeta con la imagen rota y el `404` en la pestaña **Network**.
3. **Prueba de edición de proyecto (`FE-UI-01`, `FE-UI-03`, `FE-INT-04`):**
   - En la tarjeta de un proyecto, abre el menú de tres puntos (`⋮`) y selecciona **Editar**.
   - Abre **F12 → Console** y haz clic en **Guardar**.
   - **Qué observar:** En consola se dispara `Uncaught TypeError: Cannot read properties of null (reading 'value') at asignarFondoEditar (inicio.js:129)` porque busca `#editarFondo` en vez de `#editar_fondo`. Además, el formulario envía a `POST /saveProyecto` (`/addProyecto`).
   - **📸 Evidencia (`FE-UI-01-editar-fondo-null.png`):** Captura del modal **Editar** con el error `TypeError` en `inicio.js:129` visible en la consola.

---

### Paso 5 — Tablero de Tareas (`/proyecto`): Duplicación de tarjetas, alta rápida y eliminación sin confirmación (`FE-UI-05`, `FE-INT-03`, `FE-UI-06`, `FE-UI-04`)
1. En una tarjeta de proyecto en `/inicio`, haz clic en **Ir al proyecto** (`/proyecto?id_proyecto=...`).
2. **Prueba de alta rápida vacía o incompleta (`FE-INT-03`):**
   - Haz clic en **+ Añadir tarea** en la primera columna, deja el campo de texto en blanco y pulsa **guardar**.
   - **Qué observar:** `proyecto.js` no valida el input vacío antes de llamar a `/verificarTarea`; en **Network** se observa el payload `{nombre: "", lista: "pendientes", id_proyecto: ...}` sin `estado`, `descripcion` ni `fecha_vencimiento`.
   - **📸 Evidencia (`FE-INT-03-alta-tarea-incompleta.png`):** Captura de **Network → Payload** de `/verificarTarea` mostrando el JSON incompleto o el rechazo `400` del backend.
3. **Prueba de duplicación de tareas en columnas (`FE-UI-05`):**
   - Asegúrate de tener al menos **1 tarea en «Tareas pendientes»** y **1 tarea en «En curso»** (o «Trabajos terminados») y recarga la página (`F5`).
   - **Qué observar:** La tarea de la primera columna aparece repetida varias veces porque `contenedor.append(tarjeta)` se ejecuta en cada iteración del ciclo `data.forEach` fuera de los bloques `if`.
   - **📸 Evidencia (`FE-UI-05-tareas-duplicadas.png`):** Captura del tablero mostrando la misma tarea renderizada múltiples veces en su columna, comparada con la respuesta real de `/obtenerTareas` en **Network**.
4. **Prueba de tarea que desaparece al dejar «Cambiar de lista» por defecto (`FE-UI-04`):**
   - Haz clic sobre una tarea para abrir el modal `#editarTarea`.
   - Si el `<select id="cambiarLista">` se envía en su opción `Cambiar de lista` (`value="0"`), al guardar cambios la tarea queda con `lista = "0"` en la base de datos y deja de mostrarse en las tres columnas del tablero.
   - **📸 Evidencia (`FE-UI-04-tarea-lista-cero.png`):** Captura del modal `#editarTarea` con `Cambiar de lista` seleccionado y la respuesta de `/obtenerTareas` mostrando `lista: "0"`.
5. **Prueba de eliminación sin confirmación en «En curso» / «Trabajos terminados» (`FE-UI-06`):**
   - En una tarea de la columna **Tareas pendientes**, haz clic en el icono de basura: se abre el modal `¿Está seguro que desea eliminar la tarea...?`.
   - Ahora haz clic en el icono de basura de una tarea en **En curso** o **Trabajos terminados**.
   - **Qué observar:** No se abre ningún modal de confirmación; se ejecuta `POST /eliminarTarea` inmediatamente y la tarea se borra de forma directa.
   - **📸 Evidencia (`FE-UI-06-eliminacion-sin-modal.png`):** Captura de **F12 → Elements** mostrando `onclick="eliminarTarea(...)"` en la columna «En curso» frente a `data-bs-target="#modalAlerta"` en «Pendientes» (o la petición directa a `/eliminarTarea` en **Network**).

---

### Paso 6 — Comentarios, Integridad Referencial y Sesión (`FE-INT-05`, `FE-INT-06`, `FE-INT-07`)
1. **Prueba de envío de comentario (`FE-INT-05`):**
   - Abre el detalle de una tarea, escribe un comentario, abre **F12 → Network** y pulsa el icono de enviar.
   - **Qué observar:** En **Network → Payload** de `/saveComentario` se envía `id_usuario` con el **nombre** del usuario (texto) en vez de su ID numérico, y `fecha_hora` con año de 2 dígitos (`26/10/17 ...`). Además, la respuesta de `/saveComentario` devuelve todo el HTML de la vista `proyecto` en lugar de un JSON.
   - **📸 Evidencia (`FE-INT-05-comentario-payload.png`):** Captura de **Network → Payload y Response** de la petición `POST /saveComentario`.
2. **Prueba de eliminación de proyecto con tareas asociadas (`FE-INT-06`):**
   - En `/inicio`, intenta eliminar un proyecto que tenga tareas creadas.
   - **Qué observar:** Con la restricción `FOREIGN KEY` activa en la base de datos, el backend rechaza el borrado, pero `eliminarProyecto()` en `inicio.js` no tiene bloque `error:`, dejando el modal abierto sin mostrar alerta al usuario.
   - **📸 Evidencia (`FE-INT-06-eliminar-proyecto-fk.png`):** Captura del modal de eliminación junto con el error de `/eliminarProyecto` en la pestaña **Network / Console**.
3. **Prueba de acceso al tablero sin sesión en `localStorage` (`FE-INT-07`):**
   - En **F12 → Application → Local Storage → `http://localhost:8080`**, borra la clave `usuario` y recarga `/proyecto?id_proyecto=1`.
   - **Qué observar:** En **Console** se lanza `Uncaught TypeError: Cannot read properties of null (reading 'imagen_usuario') at proyecto.js:10`, deteniendo toda la ejecución del script (no cargan ni las tareas ni los miembros del proyecto).
   - **📸 Evidencia (`FE-INT-07-localstorage-null.png`):** Captura del tablero vacío (con datos estáticos de plantilla «Perla Joceline... / Realizar infografía») y el `TypeError` en `proyecto.js:10` en consola.

---

## 4. Resumen de capturas de evidencia a entregar

| Archivo de evidencia sugerido | ID asociado | Qué debe verse claramente en la captura |
|---|---|---|
| `FE-UI-08-home-console-error.png` | FE-UI-08 | Pantalla `/` + Consola con `TypeError` en `home.html:227`. |
| `FE-UI-09-login-foco-incorrecto.png` | FE-UI-09 | Pantalla `/login` con toast «Ingrese una contraseña» y foco activo en el input de Email. |
| `FE-INT-01-login-error-handling.png` | FE-INT-01 | Pantalla `/login` + pestaña Network/Console al fallar `/verificarLogin` con credenciales inválidas. |
| `FE-UI-04-select-valor-cero.png` | FE-UI-04 | Formulario `/register` o modal de proyecto/tarea aceptando la opción por defecto `value="0"`. |
| `FE-INT-02-registro-whitelabel.png` | FE-INT-02 | Respuesta de error en `/saveUsuario` al registrar un correo duplicado o inválido. |
| `FE-UI-02-fondo-sin-extension-404.png` | FE-UI-02 | Tarjeta de proyecto con imagen rota + `GET /Images/azulCobalto 404` en Network. |
| `FE-UI-01-editar-fondo-null.png` | FE-UI-01 / FE-UI-03 / FE-INT-04 | Modal **Editar** proyecto + Consola con `TypeError` en `asignarFondoEditar (inicio.js:129)`. |
| `FE-INT-03-alta-tarea-incompleta.png` | FE-INT-03 | Pestaña Network mostrando el JSON incompleto enviado a `/verificarTarea`. |
| `FE-UI-05-tareas-duplicadas.png` | FE-UI-05 | Tablero `/proyecto` mostrando tarjetas de tareas duplicadas al haber tareas en más de una lista. |
| `FE-UI-06-eliminacion-sin-modal.png` | FE-UI-06 | Comparativa en Elements/Network de eliminación directa en «En curso» sin abrir `#modalAlerta`. |
| `FE-INT-05-comentario-payload.png` | FE-INT-05 | Pestaña Network en `/saveComentario` mostrando `id_usuario` con nombre, fecha `YY/MM/DD` y respuesta HTML. |
| `FE-INT-06-eliminar-proyecto-fk.png` | FE-INT-06 | Modal de eliminar proyecto con tareas + fallo en Network de `/eliminarProyecto` sin alerta al usuario. |
| `FE-INT-07-localstorage-null.png` | FE-INT-07 | Tablero `/proyecto` sin `usuario` en `localStorage` + `TypeError` en `proyecto.js:10` en la consola. |
