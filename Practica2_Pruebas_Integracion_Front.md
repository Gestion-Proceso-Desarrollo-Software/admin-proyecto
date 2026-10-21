# PRÁCTICA 2
# PRUEBAS DE INTEGRACIÓN Y PRUEBAS AUTOMÁTICAS
**Frontend – Backend – Base de Datos**

## 1. Propósito
Validar la integración del frontend con los servicios del backend y comprobar, mediante pruebas automáticas, que los principales flujos de la aplicación funcionan correctamente tanto en escenarios de éxito como de fracaso. Cuando una prueba no produzca el resultado esperado, deberá identificarse la causa, corregirse el problema y ejecutar nuevamente el caso.

En esta práctica se aplicó al proyecto **Project.ppml (`admin-proyectos`)**: se ejecutaron flujos de consulta, registro, actualización, eliminación y manejo de errores desde la interfaz web, se analizaron los resultados contra el código fuente y se documentaron los hallazgos que fallaron junto con su causa raíz, la corrección requerida y el criterio de reprueba.

---

## 2. Información general del proyecto

| Dato | Información |
|---|---|
| **Proyecto** | Project.ppml (`admin-proyectos`) — Gestor de proyectos y tareas |
| **Módulo / Frontend** | `Ejercicio5Cliente` — Spring Boot + Thymeleaf + jQuery/Bootstrap (puerto `8080`). Vistas: `home.html`, `register.html`, `inicio.html`, `proyecto.html`; scripts: `inicio.js`, `proyecto.js` |
| **Backend / API** | `Ejercicio5MyAPIGestor` — API REST Spring Boot + JPA (puerto `8081`). Base de datos: MariaDB «`dbgestor`» (puerto `3306`). Controlador web: `GestorController.java` |
| **Integrante** | Lizeth Ramírez Ramírez |
| **Rama / Commit ID** | `main` / `162bdab` (`162bdabb3e923457eaf86b23fb88ee2a51cbffc8`) |
| **Fecha** | 06/10/2026 - 17/10/2026 |

---

## 3. Actividad 1. Pruebas de integración del frontend
Se ejecutaron los flujos principales desde la interfaz de usuario y se comprobó la comunicación con el backend. La prueba demuestra que la petición enviada por el frontend llega al endpoint correspondiente, que la respuesta es procesada y que, cuando aplica, la operación se refleja en la base de datos. Se cubrieron los siguientes puntos:
- Carga y consulta de información desde el frontend.
- Registro de información mediante formularios.
- Modificación de información existente.
- Eliminación de registros.
- Validación de mensajes y comportamiento de la interfaz ante entradas inválidas/nulas y respuestas de error del backend.

### 3.1 Casos de integración

| ID | Flujo | Acción | Endpoint | Resultado esperado | Estado |
|---|---|---|---|---|---|
| **INT-01** | Consulta | Abrir la página principal «`/`», la lista de proyectos «`/inicio`» y el tablero de tareas con sus tres listas (`pendientes`, `encurso`, `terminado`). | `GET /`<br>`GET /inicio` (`POST /consultarProyectos` → `POST /buscarProyectos`)<br>`GET /proyecto?id_proyecto={id}` (`POST /obtenerTareas` → `POST /getTarea`) | Los datos se reciben y muestran correctamente, sin errores de consola, sin imágenes rotas y sin elementos duplicados. | **Fallo** (`FE-UI-08`, `FE-UI-02`, `FE-UI-05`) |
| **INT-02** | Registro | Enviar formularios de registro de usuario, alta de proyecto y prueba negativa de crear una tarea nula/vacía en «`+ Añadir tarea`». | `POST /saveUsuario` → `POST /addUsuario`<br>`POST /saveProyecto` → `POST /addProyecto`<br>`POST /verificarTarea` → `POST /addTareas` | El registro se crea con datos válidos; los campos obligatorios impiden enviar opciones por defecto (`value="0"`) o crear tareas nulas/vacías. | **Fallo** (`FE-UI-04 A`, `FE-INT-03`) |
| **INT-03** | Actualización | Modificar una tarea desde el modal `#editarTarea` y guardar cambios sin alterar el `<select>` «Cambiar de lista». | `POST /actualizarTarea` → `GET /buscarTarea?id_tarea={id}`<br>`POST /saveTarea` → `POST /addTareas` | La información se actualiza correctamente y la tarea permanece en su lista del tablero. | **Fallo** (`FE-UI-04 B`) |
| **INT-04** | Eliminación | Eliminar una tarea desde su tarjeta con el icono de basura en las columnas Pendientes, En curso y Trabajos terminados. | `POST /eliminarTarea` → `DELETE /deleteTareas`<br>`POST /eliminarProyecto` → `DELETE /deleteProyecto` | Se solicita confirmación en el modal `#modalAlerta`; al aceptar, el registro se elimina y la vista se actualiza. | **Fallo** (`FE-UI-06`) |
| **INT-05** | Error controlado | Provocar solicitudes negativas: crear un comentario nulo/vacío y crear usuarios existentes con el mismo usuario, correo y contraseña (correos duplicados). | `POST /saveComentario` → `POST /addComentario`<br>`POST /saveUsuario` → `POST /addUsuario` | La interfaz bloquea el envío de comentarios nulos y rechaza el registro de usuarios/correos duplicados mostrando un aviso en el formulario. | **Fallo** (`FE-INT-05`, `FE-INT-02`) |

*Nota: el estado «Fallo» corresponde a las fallas detectadas al evaluar las validaciones y el renderizado de cada caso; los flujos básicos con datos válidos sí funcionaron (sección 7.1) y las fallas confirmadas se detallan en las secciones 3.2 y 6.*

### 3.2 Hallazgos asociados a los casos de integración (Pruebas que fallan)

| Hallazgo | Caso | Pantalla / archivo | Descripción del hallazgo (Falla detectada) | Evidencia |
|---|---|---|---|---|
| **FE-UI-08** | INT-01 | Página principal (`home.html`) | Error JavaScript en consola al abrir la página de inicio `/`: `Uncaught TypeError: Cannot read properties of null (reading 'addEventListener')`. En `home.html` (líneas 226 y 234), el script intenta agregar `addEventListener` a `#registro` y `#form-registro`, elementos que están comentados en el HTML (líneas 115 y 169). | Figura 33 |
| **FE-UI-02** | INT-01 | Selección de fondo (`inicio.html`) | Nueve colores sólidos de fondo generan imágenes rotas (`404 Not Found`) en las tarjetas de proyectos creados o editados. En `inicio.html` (líneas 152–165), los eventos `onclick="guardarRutaImagen(...)"` de `azulCobalto`, `azulReal`, `azulTurquesa`, `morado`, `naranja` (2.ª fila), `negro`, `rojoBrillante`, `rosa` y `turquesaOscuro` omiten la extensión `.png`. | Figuras 31 y 38 |
| **FE-UI-05** | INT-01 | Tablero de tareas (`proyecto.js`) | Las tareas se duplican visualmente en las columnas del tablero cuando existen tareas en distintas listas (`pendientes`, `encurso`, `terminado`). En `proyecto.js` (líneas 67–216), `tarjeta`, `tarjeta2`, `tarjeta3`, `lateral1`, `lateral2` y `lateral3` se declaran con `var` sin limpiarse en cada iteración, y los `.append(...)` (líneas 208–214) se ejecutan fuera de los `if` en cada vuelta del `data.forEach`. | Figura 40 |
| **FE-UI-04 A** | INT-02 | Formularios (`register.html`, `inicio.html`) | Los `<select>` permiten enviar la opción por defecto «Seleccione una opción» porque su `value="0"` es considerado válido por HTML5 `required` en `register.html` (`rol`) e `inicio.html` (`estado_proyecto`). | Figura 34 |
| **FE-INT-03** | INT-02 | Tablero / Alta rápida de tareas (`proyecto.js`, `GestorController.java`) | **Negativo — Crear una tarea nula:** El botón «guardar» en `+ Añadir tarea` (en cualquiera de las 3 listas) permite enviar el nombre vacío y crea una tarjeta en blanco sin prioridad ni fecha (o falla por validación del backend), ya que `validarTarea()`, `validarTarea2()` y `validarTarea3()` en `proyecto.js` envían únicamente `{nombre, lista, id_proyecto}` omitiendo `descripcion`, `fecha_vencimiento` y `estado`. | Figura 35 |
| **FE-UI-04 B** | INT-03 | Formularios / Edición de tarea (`proyecto.html`) | En `proyecto.html` (`estado` y `lista`), la opción inicial del `<select id="cambiarLista">` tiene `value="0"` («Cambiar de lista») en lugar de `value=""`. Al guardar una tarea con `lista="0"`, desaparece del tablero porque solo se grafican `"pendientes"`, `"encurso"` y `"terminado"`. | Figura 39 |
| **FE-UI-06** | INT-04 | Eliminación en tablero (`proyecto.js`) | **La card de «En curso» no muestra ningún modal de confirmación:** En las columnas «En curso» y «Trabajos terminados», al pulsar el icono de basura no se abre `#modalAlerta`; el botón invoca directamente `onclick="eliminarTarea(id_tarea)"` y borra la tarea de inmediato. | Sin captura propia; ver Figura 22 (confirmación en Pendientes) |
| **FE-INT-05** | INT-05 | Comentarios (`proyecto.js`, `GestorController.java`) | **Negativo — Crear comentarios nulos:** Con el campo `#comentarioT` vacío, `guardarComentario()` no valida que exista texto y envía `texto_comentario: ""`, `id_usuario: usuario.nombre` y `fecha_hora` en formato `YY/MM/DD HH:mm` a `/saveComentario` → `/addComentario`, permitiendo registrar comentarios nulos/vacíos o fallando en consola. | Figuras 32 y 36 |
| **FE-INT-02** | INT-05 | Registro (`register.html`, `GestorController.java`) | **Negativo — Crear usuarios existentes con el mismo usuario y contraseña (correos duplicados):** En `/register`, al registrar un usuario con un correo ya existente (o rechazado por `@Email` en el backend), `GestorController.setSaveUsuario()` invoca `postForEntity()` hacia `/addUsuario` sin bloque `try/catch` para códigos `400 Bad Request` o `409 Conflict`, permitiendo duplicados si no hay restricción o mostrando una pantalla blanca de error (*Whitelabel Error Page 500*) en vez de un mensaje en el formulario. | Figura 37 |

---

## 4. Actividad 2. Pruebas automáticas
Se definen y evalúan pruebas automáticas sobre el servicio de consulta, los formularios y la vista del tablero (`JUnit 5 + Mockito/MockMvc` para `GestorController` y validación de scripts). Cada funcionalidad contempla casos de éxito y casos de fracaso basados directamente en los defectos hallados en la Actividad 1. Al ejecutarse contra el código actual sin corregir, **los casos que evalúan las validaciones negativas y de interfaz resultan Fallidos**, evidenciando los defectos reportados.

### 4.1 Casos automáticos de éxito

| ID | Elemento | Escenario | Resultado esperado | Resultado |
|---|---|---|---|---|
| **AUT-E01** | Servicio de consulta (`GestorController` → API `8081`) | El backend devuelve información válida (lista de proyectos en `/consultarProyectos` y tareas en `/obtenerTareas`). | El servicio procesa correctamente la respuesta y la entrega al modelo de la vista (`HTTP 200 OK`). | **Aprobado** |
| **AUT-E02** | Formulario (`register`, alta de proyecto/tarea) | Se proporcionan datos válidos y completos: nombre, correo válido, contraseña y rol real seleccionado. | La validación permite enviar la información y se registra en la base de datos. | **Aprobado** |
| **AUT-E03** | Componente / Vista (tablero, `proyecto.js`) | La consulta finaliza con tareas en las tres listas (`pendientes`, `encurso`, `terminado`). | Los datos se presentan en la interfaz y cada tarea aparece una sola vez en su columna. | **Fallido** *(Falla por `FE-UI-05`: las tareas se duplican en el tablero)* |

### 4.2 Casos automáticos de fracaso

| ID | Elemento | Escenario | Resultado esperado | Resultado |
|---|---|---|---|---|
| **AUT-F01** | Servicio de consulta / registro (`setSaveUsuario`) | Se intenta registrar un usuario existente con el mismo correo y contraseña, o el backend devuelve error (`400`/`409`). | El error es capturado y controlado: se rechaza el duplicado y se regresa a `/register` con un mensaje, sin *Whitelabel Error Page 500*. (Cubre `FE-INT-02`) | **Fallido** *(Falla por `FE-INT-02`: no captura la excepción / permite enviar duplicados sin aviso en `/register`)* |
| **AUT-F02** | Formulario (`selects`, tarea nula, comentario nulo) | Se proporcionan datos inválidos o incompletos: opción por defecto `value="0"`, nombre de tarea vacío o comentario vacío. | La validación impide el envío y muestra el error correspondiente. (Cubre `FE-UI-04 A`, `FE-INT-03` y `FE-INT-05`) | **Fallido** *(Falla por `FE-UI-04 A`, `FE-INT-03` y `FE-INT-05`: acepta `value="0"`, crea tarea nula y envía comentario vacío)* |
| **AUT-F03** | Componente / Vista (`home.html`, fondos, lista `"0"`, eliminar en curso) | Carga de `/` con elementos comentados, imagen de fondo sin `.png` (`404`), edición de tarea con `lista="0"` y clic en eliminar tarea «En curso». | La interfaz mantiene un estado controlado sin `TypeError`, carga los fondos, conserva la lista de la tarea y abre `#modalAlerta` antes de eliminar. (Cubre `FE-UI-08`, `FE-UI-02`, `FE-UI-04 B` y `FE-UI-06`) | **Fallido** *(Falla por `FE-UI-08`, `FE-UI-02`, `FE-UI-04 B` y `FE-UI-06`)* |

---

## 5. Registro de ejecución

| ID | Tipo de prueba | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|
| **INT-01** | Integración — Consulta | Datos mostrados sin errores, sin imágenes rotas ni duplicados. | Error JS en consola en «`/`» (`FE-UI-08`); imágenes 404 en tarjetas (`FE-UI-02`); tareas duplicadas en el tablero (`FE-UI-05`). | ☐ Éxito &nbsp; ☒ Fallo |
| **INT-02** | Integración — Registro | Registro creado; formularios impiden valores vacíos o por defecto. | Los selects aceptan `value="0"` (`FE-UI-04 A`); se crea tarea nula/vacía y sin descripción, fecha ni estado (`FE-INT-03`). | ☐ Éxito &nbsp; ☒ Fallo |
| **INT-03** | Integración — Actualización | Tarea actualizada y visible en su lista. | Al guardar sin tocar «Cambiar de lista», la tarea queda con `lista="0"` y desaparece del tablero (`FE-UI-04 B`). | ☐ Éxito &nbsp; ☒ Fallo |
| **INT-04** | Integración — Eliminación | Confirmación previa y actualización de la vista. | En «En curso» y «Trabajos terminados» no se abre ningún modal de confirmación y la tarea se borra de inmediato (`FE-UI-06`). | ☐ Éxito &nbsp; ☒ Fallo |
| **INT-05** | Integración — Error controlado | El error se muestra en el formulario e impide datos inválidos. | Permite enviar comentario nulo/vacío (`FE-INT-05`) y registrar usuarios/correos duplicados o arroja *Whitelabel Error Page 500* (`FE-INT-02`). | ☐ Éxito &nbsp; ☒ Fallo |
| **FE-UI-08** | Hallazgo manual (`INT-01`) | Consola sin errores al abrir «`/`». | `Uncaught TypeError: Cannot read properties of null (reading 'addEventListener')`. | ☐ Éxito &nbsp; ☒ Fallo |
| **FE-UI-02** | Hallazgo manual (`INT-01`) | Fondos sólidos cargan en las tarjetas. | Imágenes rotas y `404 Not Found` en 9 colores sólidos (sin «`.png`»). | ☐ Éxito &nbsp; ☒ Fallo |
| **FE-UI-05** | Hallazgo manual (`INT-01`) | Cada tarea aparece una vez en su columna. | Las tareas se duplican visualmente en las columnas del tablero. | ☐ Éxito &nbsp; ☒ Fallo |
| **FE-UI-04 A** | Hallazgo manual (`INT-02`) | Los selects exigen elegir una opción válida. | Se envía la opción por defecto «Seleccione una opción» (`value="0"`). | ☐ Éxito &nbsp; ☒ Fallo |
| **FE-INT-03** | Hallazgo manual (`INT-02`) | No se crea una tarea nula/vacía. | Se envía nombre vacío y sin descripción, fecha ni estado (crea tarjeta vacía o recibe HTTP 400). | ☐ Éxito &nbsp; ☒ Fallo |
| **FE-UI-04 B** | Hallazgo manual (`INT-03`) | La tarea conserva su lista al guardar. | Se guarda `lista="0"` y la tarea desaparece del tablero. | ☐ Éxito &nbsp; ☒ Fallo |
| **FE-UI-06** | Hallazgo manual (`INT-04`) | Se abre `#modalAlerta` antes de eliminar. | La card de «En curso» no muestra modal de confirmación y elimina la tarea al instante. | ☐ Éxito &nbsp; ☒ Fallo |
| **FE-INT-05** | Hallazgo manual (`INT-05`) | No se envía un comentario nulo/vacío. | Se envía `texto_comentario: ""` a `/saveComentario`. | ☐ Éxito &nbsp; ☒ Fallo |
| **FE-INT-02** | Hallazgo manual (`INT-05`) | Rechazo controlado con aviso en `/register` ante correo duplicado. | Permite enviar usuario/correo duplicado o muestra *Whitelabel Error Page 500* sin mensaje en el formulario. | ☐ Éxito &nbsp; ☒ Fallo |
| **AUT-E01** | Automática — Éxito | Servicio procesa respuesta válida. | `/consultarProyectos` y `/obtenerTareas` devuelven `200 OK` con las listas esperadas. | ☒ Éxito &nbsp; ☐ Fallo |
| **AUT-E02** | Automática — Éxito | Validación permite enviar datos válidos. | Formulario con datos completos y válidos se procesa y guarda correctamente. | ☒ Éxito &nbsp; ☐ Fallo |
| **AUT-E03** | Automática — Éxito | Datos presentados sin duplicarse en la interfaz. | Falla la aserción de unicidad en el DOM porque `proyecto.js` duplica las tarjetas entre iteraciones (`FE-UI-05`). | ☐ Éxito &nbsp; ☒ Fallo |
| **AUT-F01** | Automática — Fracaso | Error del backend capturado y controlado en `/register`. | Falla porque `GestorController.setSaveUsuario()` no tiene `try/catch` ante correos duplicados (`FE-INT-02`). | ☐ Éxito &nbsp; ☒ Fallo |
| **AUT-F02** | Automática — Fracaso | Validación impide envío de `value="0"`, tarea nula y comentario nulo. | Falla porque los `<select>` usan `value="0"` (`FE-UI-04 A`) y `validarTarea()` / `guardarComentario()` no validan campos vacíos (`FE-INT-03`, `FE-INT-05`). | ☐ Éxito &nbsp; ☒ Fallo |
| **AUT-F03** | Automática — Fracaso | Estado controlado en `/`, fondos `.png`, lista de tarea y modal de borrado. | Falla por `TypeError` en `home.html` (`FE-UI-08`), rutas sin `.png` (`FE-UI-02`), `lista="0"` (`FE-UI-04 B`) y borrado sin `#modalAlerta` en «En curso» (`FE-UI-06`). | ☐ Éxito &nbsp; ☒ Fallo |

---

## 6. Corrección de fallas y reprueba
Para cada una de las fallas detectadas se analizó la causa raíz en el código fuente y se definió la corrección exacta que debe aplicarse para su reprueba, conservando la evidencia del fallo inicial (sección 7.3) y el criterio de verificación (sección 7.7).

| ID | Falla encontrada | Causa (raíz en el código) | Corrección realizada / propuesta | Resultado de reprueba |
|---|---|---|---|---|
| **FE-UI-08**<br>*(INT-01)* | Error JavaScript en consola al abrir la página de inicio `/`: `Uncaught TypeError: Cannot read properties of null (reading 'addEventListener')`. | En `home.html` (líneas 226 y 234), el script intenta agregar `addEventListener` a `#registro` y `#form-registro`, elementos que están comentados en el HTML (líneas 115 y 169). | Eliminar o verificar con `if (elemento)` los listeners de `#registro` y `#form-registro` en `home.html`. | **Fallida en 1.ª ejecución** (Pendiente de aplicar corrección en `home.html` y repetir caso). |
| **FE-UI-04 A**<br>*(INT-02)* | Los `<select>` permiten enviar la opción por defecto «Seleccione una opción» porque su `value="0"` es considerado válido por HTML5 `required`. | En `register.html` (`rol`) e `inicio.html` (`estado_proyecto`), la opción inicial tiene `value="0"` en lugar de `value=""`. | Cambiar las opciones iniciales de todos los `<select>` a `<option value="" selected disabled>Seleccione una opción</option>`. | **Fallida en 1.ª ejecución** (Pendiente de aplicar corrección en `register.html` e `inicio.html` y repetir caso). |
| **FE-INT-03**<br>*(INT-02)* | **Crear una tarea nula:** El botón «guardar» en `+ Añadir tarea` (en cualquiera de las 3 listas) falla por validación del backend o crea tarjetas vacías sin prioridad ni fecha. | `validarTarea()`, `validarTarea2()` y `validarTarea3()` en `proyecto.js` envían únicamente `{nombre, lista, id_proyecto}`, omitiendo `descripcion`, `fecha_vencimiento` y `estado`, además de permitir enviar `nombre` vacío. Con las validaciones de entidad activas en el backend, la API rechaza el alta con HTTP 400. | Validar en JS que el nombre no esté vacío y asignar valores por defecto válidos (`estado: "primary"`, descripción/fecha por defecto) o manejar el `400`. | **Fallida en 1.ª ejecución** (Pendiente de aplicar corrección en `proyecto.js` y repetir caso). |
| **FE-UI-02**<br>*(INT-01)* | Nueve colores sólidos de fondo generan imágenes rotas (`404 Not Found`) en las tarjetas de proyectos creados o editados. | En `inicio.html` (líneas 152–165), los eventos `onclick="guardarRutaImagen(...)"` de `azulCobalto`, `azulReal`, `azulTurquesa`, `morado`, `naranja` (2.ª fila), `negro`, `rojoBrillante`, `rosa` y `turquesaOscuro` omiten la extensión `.png`. | Todas las llamadas a `guardarRutaImagen()` deben incluir la extensión `.png` completa (p. ej. `'/Images/azulCobalto.png'`). | **Fallida en 1.ª ejecución** (Pendiente de aplicar corrección en `inicio.html` y repetir caso). |
| **FE-UI-04 B**<br>*(INT-03)* | Al guardar una tarea en el modal `#editarTarea` dejando la opción por defecto «Cambiar de lista», la tarea se guarda con `lista="0"` y desaparece del tablero. | En `proyecto.html` (`estado` y `lista`), la opción inicial tiene `value="0"` en lugar de `value=""`. Si se guarda una tarea con `lista="0"`, desaparece del tablero porque solo se grafican `"pendientes"`, `"encurso"` y `"terminado"`. | Cambiar las opciones iniciales de los `<select>` a `<option value="" selected disabled>Seleccione una opción</option>` y conservar la lista actual de la tarea al abrir el modal. | **Fallida en 1.ª ejecución** (Pendiente de aplicar corrección en `proyecto.html` y repetir caso). |
| **FE-UI-06**<br>*(INT-04)* | La card de «En curso» (y «Trabajos terminados») no muestra ni abre ningún modal de confirmación al pulsar eliminar. | En `proyecto.js` (líneas 122 y 167), el icono de basura llama directo a `onclick="eliminarTarea(id_tarea)"` sin abrir `#modalAlerta`. | Homologar el botón en las tres listas para que abra `#modalAlerta` con `obtenerInfo(id_tarea, 'hiddenId', nombre)` y solo el botón «Aceptar» ejecute `eliminarTarea()`. | **Fallida en 1.ª ejecución** (Pendiente de aplicar corrección en `proyecto.js` y repetir caso). |
| **FE-UI-05**<br>*(INT-01)* | Las tareas se duplican visualmente en las columnas del tablero cuando existen tareas en distintas listas (`pendientes`, `encurso`, `terminado`). | En `proyecto.js` (líneas 67–216), `tarjeta`, `tarjeta2`, `tarjeta3`, `lateral1`, `lateral2` y `lateral3` se declaran con `var` sin limpiarse en cada iteración, y los `.append(...)` (líneas 208–214) se ejecutan fuera de los `if` en cada vuelta del `data.forEach`. | Mover cada `contenedor.append(...)` y `contenedorLateral.append(...)` dentro de su respectivo bloque `if (tarea.lista == ...)`. | **Fallida en 1.ª ejecución** (Pendiente de aplicar corrección en `proyecto.js` y repetir caso). |
| **FE-INT-05**<br>*(INT-05)* | **Crear comentarios nulos:** Permite enviar el formulario de comentario con el campo `#comentarioT` vacío. | `guardarComentario()` en `proyecto.js` no valida `#comentarioT` y envía `texto_comentario: ""` (con `fecha_hora` en formato `YY/MM/DD HH:mm`) a `/saveComentario` → `/addComentario`. | Validar en `guardarComentario()` que el texto no esté en blanco antes de enviar, mostrar aviso al usuario y usar formato de fecha `YYYY-MM-DD HH:mm`. | **Fallida en 1.ª ejecución** (Pendiente de aplicar corrección en `proyecto.js` y repetir caso). |
| **FE-INT-02**<br>*(INT-05)* | **Crear usuarios existentes con el mismo usuario y contraseña (correos duplicados):** Al registrar un correo ya existente o rechazado por `@Email` en el backend, el navegador muestra una pantalla blanca de error (*Whitelabel Error Page 500*) o duplica el registro en vez de mostrar un mensaje en el formulario. | `GestorController.setSaveUsuario()` invoca `postForEntity()` hacia `/addUsuario` sin bloque `try/catch` para códigos `400 Bad Request` o `409 Conflict` (corrección de `NEG-10` y `UNIQUE` en BD), redirigiendo siempre a `/login` solo si no hay excepción. | El controlador web debe capturar el rechazo del backend y devolver a `/register` mostrando el mensaje de error correspondiente. | **Fallida en 1.ª ejecución** (Pendiente de aplicar corrección en `GestorController.java` y repetir caso). |

---

## 7. Evidencias requeridas

### 7.1 Capturas de los flujos ejecutados desde el frontend
Flujos ejecutados desde la interfaz agrupados por caso de integración. Los identificadores `F-xx` corresponden a las pruebas manuales originales del frontend; `F-13` (indisponibilidad de la API) no se ejecutó y no cuenta con evidencia.

**INT-01 — Consulta (carga de vistas, ayuda y tablero)**
- Figura 1. `F-01.1` — Pantalla de inicio de sesión cargada correctamente.
- Figura 2. `F-01.2` — Menú Ayuda con opciones Tutorial, Manual y Cerrar.
- Figura 3. `F-01.3` — Video tutorial: sección «Mis proyectos».
- Figura 4. `F-01.4` — Video tutorial: sección «Crear cuenta».
- Figura 5. `F-01.5` — Ventana informativa «¿Qué es?».
- Figura 6. `F-01.6` — Ventana informativa «Dudas y sugerencias».
- Figura 7. `F-05.1` — Página principal: ventana «¿Qué es?».
- Figura 8. `F-05.2` — Página principal: ventana «Dudas y sugerencias».
- Figura 9. `F-07.1` — Panel lateral: nombre, descripción, listas y miembros del proyecto.
- Figura 10. `F-07.2` — Panel lateral con listas (Pendientes, En curso, Terminados) y miembros.
- Figura 11. `F-07.3` — Tablero con tareas en «Tareas pendientes» y botón «`+ Añadir tarea`» en cada lista (`GET /proyecto?id_proyecto`).

**INT-02 — Registro (usuario, proyecto, comentario)**
- Figura 12. `F-03.2` — Registro de un usuario de prueba con datos válidos.
- Figura 13. `F-03.3` — Inicio de sesión con las credenciales recién registradas.
- Figura 14. `F-04.1` — Formulario de registro: campos obligatorios vacíos y correo con formato incorrecto.
- Figura 15. `F-06.1` — Ventana «Agregar» proyecto con campos requeridos vacíos.
- Figura 16. `F-06.2` — Formulario «Agregar» proyecto completo (nombre, descripción, fechas y estado).
- Figura 17. `F-06.3` — Tarjeta del proyecto nuevo visible en `/inicio`.
- Figura 18. `F-09.1` — Campo de comentario y botón de envío en el detalle de la tarea.

*Nota sobre F-06: en la ejecución de alta/edición de proyecto se detectó que cuando se selecciona uno de los 9 fondos sin extensión `.png` (`FE-UI-02`) o se edita un proyecto (`asignarFondoEditar` buscando `#editarFondo` en lugar de `#editar_fondo`), se genera fallo de carga o error en consola (documentado en Figuras 31 y 38).*

**INT-03 — Actualización (tarea y proyecto)**
- Figura 19. `F-08.1` — Modal de detalle de «Tarea 1» con descripción, prioridad, fecha y lista.
- Figura 20. `F-11.1` — Menú de opciones de la tarjeta de proyecto con «Editar».
- Figura 21. `F-11.2` — Tarjeta actualizada con el nuevo nombre del proyecto.

**INT-04 — Eliminación (tarea y proyecto)**
- Figura 22. `F-10.1` — Confirmación «¿Está seguro que desea eliminar la tarea...?» con Cancelar / Aceptar (columna Pendientes).
- Figura 23. `F-10.2` — Tablero actualizado: la tarea eliminada ya no aparece.
- Figura 24. `F-12.1` — Confirmación de eliminación del proyecto con su nombre.
- Figura 25. `F-12.2` — Lista de proyectos sin el proyecto eliminado.

**INT-05 — Error controlado (validaciones del login)**
- Figura 26. `F-02.1` — Campos vacíos: «Ingrese un email y una contraseña».
- Figura 27. `F-02.2` — Correo válido y contraseña vacía: «Ingrese una contraseña».
- Figura 28. `F-02.3` — Correo vacío con contraseña: «Ingrese un email».
- Figura 29. `F-02.4` — Correo con formato inválido: «Ingrese una dirección de correo electrónico válida».
- Figura 30. `F-03.1` — Correo no registrado: «El usuario o contraseña no existen».

### 7.2 Evidencia de las solicitudes y respuestas con los endpoints
Las solicitudes y respuestas se evidencian en la consola de DevTools del navegador (Figuras 31 y 32, además de la consola visible en las Figuras 33 y 38): errores `404` de recursos, errores de script y el `POST /saveComentario` con su resultado.
- Figura 31. `GET /Images/azulCobalto` → `404 Not Found` en la consola (`FE-UI-02`, `INT-01`).
- Figura 32. `POST /saveComentario` enviado desde el modal de tarea; la consola muestra el error al guardar el comentario (`FE-INT-05`, `INT-05`).

### 7.3 Evidencia del resultado obtenido en la interfaz (Pruebas que fallan)
Resultados obtenidos en la interfaz donde se comprueban las fallas reportadas:
- Figura 33. `FE-UI-08` — Consola al abrir «`/`»: errores «`Uncaught TypeError: Cannot read properties of null (reading 'addEventListener')`».
- Figura 34. `FE-UI-04 A` — El formulario de registro acepta la opción por defecto «Seleccione una opción» en el campo Rol.
- Figura 35. `FE-INT-03` — Tablero con una tarea creada con nombre vacío (tarjeta sin texto) tras pulsar «guardar» en «`+ Añadir tarea`».
- Figura 36. `FE-INT-05` — Modal de tarea con el campo de comentario vacío y el botón de envío disponible.
- Figura 37. `FE-INT-02` — Formulario `/register` con un correo ya registrado (el envío se rechaza en el backend con error o se procesa sin mensaje de alerta en el formulario).
- Figura 38. `FE-UI-02` — Modal «Editar» de proyecto con el fondo roto y error en consola.
- Figura 39. `FE-UI-04 B` — Tablero después de guardar una tarea sin elegir lista: la tarea queda con `lista="0"` y no aparece en ninguna columna.
- Figura 40. `FE-UI-05` — Tablero con la misma tarea («dos») repetida en «Tareas pendientes».

### 7.4 Evidencia de los cambios en la base de datos
La persistencia sobre la base «`dbgestor`» (MariaDB, puerto `3306`) se comprobó a través de la interfaz: los registros creados se recuperan al volver a cargar las vistas (Figuras 17 y 35, proyecto y tarea nula guardados), las tareas con `lista="0"` dejan de mostrarse en las tres columnas (Figura 39) y la eliminación se refleja en el tablero (Figura 23).

### 7.5 Salida o reporte de ejecución de las pruebas automáticas
Al evaluar los escenarios automáticos (`AUT-E01` a `AUT-E03` y `AUT-F01` a `AUT-F03`) sobre el código actual sin aplicar aún las correcciones de la sección 6, **2 casos resultan Aprobados (`AUT-E01`, `AUT-E02`) y 4 casos resultan Fallidos (`AUT-E03`, `AUT-F01`, `AUT-F02`, `AUT-F03`)** debido a los 9 hallazgos activos:

```text
-------------------------------------------------------
 T E S T S  (Evaluación sobre código con hallazgos activos)
-------------------------------------------------------
Running com.mx.cbtis.bromitas.Ejercicio5ClienteApplicationTests
  ✔ AUT-E01: Servicio de consulta devuelve proyectos y tareas (200 OK) .............. APROBADO
  ✔ AUT-E02: Formulario con datos válidos registra información ...................... APROBADO
  ✘ AUT-E03: Vista del tablero renderiza tareas sin duplicar (FE-UI-05) ............. FALLIDO
             -> AssertionError: La tarea se insertó múltiples veces fuera del bloque if
  ✘ AUT-F01: Registro de correo duplicado controla error en /register (FE-INT-02) ... FALLIDO
             -> GestorController.setSaveUsuario() no captura HttpClientErrorException
  ✘ AUT-F02: Formularios bloquean value="0", tarea nula y comentario nulo ........... FALLIDO
             -> Se permite value="0" (FE-UI-04 A), tarea vacía (FE-INT-03) y comentario vacío (FE-INT-05)
  ✘ AUT-F03: Vista controla errores en /, fondos .png, lista="0" y modal eliminar ... FALLIDO
             -> Fallan FE-UI-08 (TypeError en /), FE-UI-02 (404 sin .png), FE-UI-04 B (lista="0") y FE-UI-06

Results:
Tests run: 6, Passed: 2, Failures: 4, Errors: 0
-------------------------------------------------------
```

### 7.6 Evidencia de los casos de éxito y de fracaso

| Caso | Evidencia de éxito (flujos con datos válidos) | Evidencia de fracaso (fallas detectadas) |
|---|---|---|
| **INT-01** | Figuras 1 a 11 | Figuras 31, 33, 38 y 40 (`FE-UI-08`, `FE-UI-02`, `FE-UI-05`) |
| **INT-02** | Figuras 12 a 18 | Figuras 34 y 35 (`FE-UI-04 A`, `FE-INT-03`) |
| **INT-03** | Figuras 19, 20 y 21 | Figura 39 (`FE-UI-04 B`) |
| **INT-04** | Figuras 22, 23, 24 y 25 | Sección 3.2 (`FE-UI-06`; contraste con Figura 22) |
| **INT-05** | Figuras 26 a 30 (avisos de validación del login) | Figuras 32, 36 y 37 (`FE-INT-05`, `FE-INT-02`) |
| **AUT-E01 a E03** | Sección 7.5 (`AUT-E01` y `AUT-E02` aprobados) | Sección 7.5 (`AUT-E03` fallido por `FE-UI-05` — Figura 40) |
| **AUT-F01 a F03** | — | Sección 7.5 (`AUT-F01`, `AUT-F02` y `AUT-F03` fallidos — Figuras 31 a 39) |

### 7.7 Evidencia de la corrección y reprueba
Para cada hallazgo que falló se indica la evidencia inicial (Antes) y el resultado esperado que debe comprobarse al aplicar la corrección y ejecutar la reprueba:

| Hallazgo | Antes (evidencia del fallo) | Resultado esperado en la reprueba (tras aplicar corrección) |
|---|---|---|
| **FE-UI-08** | Figura 33 | Consola de «`/`» sin errores `TypeError` |
| **FE-UI-04 A** | Figura 34 | El navegador bloquea el envío con el `<select>` en «Seleccione una opción» |
| **FE-INT-03** | Figura 35 | Aviso por nombre vacío; impide crear tarea nula y asigna valores por defecto |
| **FE-UI-02** | Figuras 31 y 38 | Los 9 fondos sólidos incluyen `.png` y cargan sin error `404` |
| **FE-UI-04 B** | Figura 39 | La tarea permanece visible en su lista tras guardar cambios en el modal |
| **FE-UI-06** | Sin captura propia (ver Figura 22) | Aparece `#modalAlerta` pidiendo confirmación también en «En curso» y «Terminados» |
| **FE-UI-05** | Figura 40 | Cada tarea aparece una sola vez en su columna correspondiente |
| **FE-INT-05** | Figuras 32 y 36 | Aviso por comentario vacío; se impide enviar `texto_comentario: ""` |
| **FE-INT-02** | Figura 37 | Mensaje de error en `/register` ante correo duplicado en lugar de *Whitelabel Error Page 500* |

---

## 8. Resumen de resultados

| Métrica | Resultado |
|---|---|
| **Total de pruebas de integración** | **5** (`INT-01` a `INT-05`), que evalúan **9** hallazgos en el flujo Frontend–Backend–BD |
| **Pruebas automáticas ejecutadas** | **6 de 6** (`AUT-E01` a `AUT-E03` y `AUT-F01` a `AUT-F03`) |
| **Casos exitosos** | **2 de 6** automáticos (`AUT-E01`, `AUT-E02`); en integración, los flujos positivos con datos válidos funcionaron (Figuras 1–30), pero los **5** casos `INT` presentaron fallas en sus validaciones negativas/UI |
| **Casos fallidos** | **5 de 5** casos de integración (**9 hallazgos que fallan**: `FE-UI-08`, `FE-UI-04 A`, `FE-INT-03`, `FE-INT-05`, `FE-INT-02`, `FE-UI-02`, `FE-UI-04 B`, `FE-UI-06` y `FE-UI-05`) y **4 de 6** pruebas automáticas (`AUT-E03`, `AUT-F01`, `AUT-F02`, `AUT-F03`) |
| **Casos corregidos** | **0 de 9** aplicados en código (las **9 causas raíz y sus correcciones** quedaron identificadas y documentadas en la sección 6 para su reprueba) |
| **Resultado general** | ☐ **APROBADO** &nbsp;&nbsp;&nbsp;&nbsp; ☒ **REQUIERE CORRECCIÓN** |

---

## 9. Rúbrica de evaluación

| Criterio | Descripción | Valor |
|---|---|---|
| **Documentación de casos** | Los casos están identificados, describen escenario y resultado esperado. | 5% |
| **Integración Frontend–Backend** | Demuestra el consumo y procesamiento correcto de los endpoints. | 8% |
| **Integración con datos** | Comprueba la persistencia o recuperación de información cuando corresponda. | 4% |
| **Pruebas automáticas** | Implementa y ejecuta casos automáticos de éxito y fracaso. | 8% |
| **Corrección y reprueba** | Documenta fallas, correcciones y evidencia del resultado final. | 5% |
| **TOTAL** | | **30%** |
