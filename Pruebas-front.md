# Manual de pruebas del front-end

Esta guía describe cómo probar manualmente la interfaz web de **Project.ppml** en el navegador y comprobar que sus formularios y pantallas se comuniquen correctamente con la API.

Para verificar directamente en MariaDB que los registros se guarden y se relacionen correctamente, consulta también [Pruebas manuales de la base de datos](Pruebas-base-datos.md).

## 1. Requisitos y preparación

- Java y Maven disponibles (el repositorio incluye el wrapper `mvnw.cmd`).
- MariaDB encendida y configurada para la API.
- Un navegador actualizado. Las capturas de pantalla y la consola de desarrollador son opcionales, pero útiles para registrar defectos.
- Datos de prueba y una contraseña inventada para el entorno local. No uses credenciales personales.

El front depende de la API para iniciar sesión, registrar usuarios y proyectos, y cargar proyectos, tareas, miembros y comentarios. Deja ambos módulos ejecutándose.

**Terminal 1 — API** (desde la raíz del repositorio):

```powershell
Set-Location ".\Ejercicio5MyAPIGestor"
.\mvnw.cmd spring-boot:run
```

La API debe estar disponible en <http://localhost:8081>. Fuera de las pruebas automatizadas, necesita MariaDB y la base `dbgestor`.

**Terminal 2 — aplicación web** (desde la raíz del repositorio):

```powershell
Set-Location ".\Ejercicio5MyAPIGestor"
.\mvnw.cmd -f ..\Ejercicio5Cliente\pom.xml spring-boot:run
```

Abre <http://localhost:8080/>. Si la consola de alguna terminal muestra errores, resuélvelos antes de continuar.

## 2. Criterios generales

1. Ejecuta los casos en orden cuando dependan de datos creados en un caso anterior.
2. Para los casos con persistencia, confirma el resultado visualmente y, si hay dudas, consulta el registro mediante los endpoints de la API descritos en [`PRUEBAS.md`](PRUEBAS.md).
3. Los registros se guardan en la base de datos; elimina únicamente los registros creados para estas pruebas.
4. En cada caso anota **Aprobado**, **Fallido** o **No ejecutado**, junto con el resultado observado.
5. Un error de conexión con la API no es un resultado de validación del formulario: revisa que ambos servicios sigan encendidos.

## 3. Pruebas manuales

### F-01 — Navegación y recursos estáticos

1. Abre <http://localhost:8080/>.
2. Comprueba que aparezcan la página principal, el logotipo y las imágenes sin iconos de error.
3. Selecciona **Ingresar** y confirma que abre la pantalla de inicio de sesión.
4. Regresa a la página principal, abre **Ayuda** y comprueba que aparezcan los enlaces **Tutorial** y **Manual**.
5. Abre ambos enlaces y verifica que las vistas carguen y que el video del tutorial pueda reproducirse.

**Resultado esperado:** las vistas y los recursos estáticos se muestran sin errores 404. Los enlaces de ayuda abren sus respectivas páginas.

### F-02 — Validaciones del inicio de sesión

1. En `/login`, pulsa **Iniciar sesión** con ambos campos vacíos.
2. Escribe un correo válido y deja la contraseña vacía; vuelve a pulsar el botón.
3. Deja el correo vacío y escribe una contraseña.
4. Escribe un correo con formato inválido, por ejemplo `correo-invalido`, y una contraseña.

**Resultado esperado:** cada caso muestra el aviso correspondiente y no inicia sesión. Cuando corresponda, el foco vuelve al campo que requiere atención.

### F-03 — Inicio de sesión inválido y válido

1. En `/login`, intenta acceder con un correo que no esté registrado y una contraseña cualquiera.
2. Comprueba que se muestre el aviso de usuario o contraseña incorrectos y que la pantalla no redirija a `/inicio`.
3. Registra un usuario de prueba siguiendo F-04.
4. Vuelve a `/login` e inicia sesión con el correo y la contraseña recién registrados.
5. Comprueba que se abra `/inicio` y que los datos del usuario estén disponibles para el resto de la interfaz.

**Resultado esperado:** las credenciales incorrectas no permiten entrar; las correctas redirigen a la pantalla de proyectos.

### F-04 — Registro de usuario

1. Desde el enlace de registro del inicio de sesión, abre `/register`.
2. Intenta enviar el formulario sin nombre, correo o contraseña. Prueba también un correo con formato incorrecto.
3. Completa los campos con datos de prueba únicos: nombre, correo válido, contraseña inventada y rol **Estudiante**, **Docente** u **Otro**.
4. Pulsa **Registrar**.
5. Comprueba que la aplicación redirija a `/login`.
6. Inicia sesión con el usuario recién creado para comprobar que se guardó correctamente.

**Resultado esperado:** los campos de nombre, correo y contraseña se validan en el front; con información válida, el usuario se registra y se puede iniciar sesión.

**Nota:** el formulario presenta un rol inicial «Seleccione una opción», pero el front no garantiza que se rechace ese valor si se envía sin elegir un rol. Selecciona uno de los roles reales para esta prueba.

### F-05 — Elementos informativos de la página principal

1. Desde `/`, abre **Acerca del Proyecto** y comprueba que aparezca su ventana informativa.
2. Cierra la ventana y abre **Contacto**.
3. Abre **Ayuda** y usa **Cerrar** para cerrar la ventana sin navegar.
4. Repite en un ancho de pantalla reducido y comprueba que los controles sigan siendo accesibles.

**Resultado esperado:** las ventanas informativas se abren y cierran sin bloquear la navegación.

### F-06 — Alta y visualización de proyecto

1. Inicia sesión y confirma que estás en `/inicio`.
2. Pulsa **Agregar**.
3. Envía el formulario vacío para comprobar que no se acepte.
4. Completa nombre, descripción, fecha de inicio, fecha de finalización y estado.
5. Abre **Elegir un color de fondo**, selecciona una imagen o color y guarda el proyecto.
6. Comprueba que la aplicación regrese a `/inicio` y que la tarjeta muestre el nombre, descripción, fechas, estado y fondo elegidos.
7. Pulsa **Ir al proyecto** en esa tarjeta.

**Resultado esperado:** los campos requeridos evitan el envío vacío; el proyecto se guarda, aparece en la lista y abre su tablero.

**Nota:** si no se elige un fondo, la pantalla intenta asignar una imagen blanca predeterminada. Algunas opciones de fondo podrían apuntar a rutas incompletas; si una imagen no carga, registra el nombre de la opción y la URL solicitada.

### F-07 — Consulta del tablero y listas de tareas

1. En el tablero del proyecto, comprueba el nombre y descripción del proyecto en el panel lateral.
2. Revisa que aparezcan las tres listas: **Tareas pendientes**, **En curso** y **Trabajos terminados**.
3. Comprueba que el panel lateral de miembros muestre los usuarios registrados.
4. Usa **+ Añadir tarea** en cada una de las tres listas, escribe un nombre y pulsa **guardar**.
5. Comprueba que cada tarea aparezca en la lista donde se creó.
6. Recarga el tablero y confirma que las tareas sigan asociadas al proyecto y en su lista correspondiente.

**Resultado esperado:** el tablero carga los datos del proyecto y usuarios desde la API; las tareas nuevas se muestran en la columna seleccionada y persisten al recargar.

**Nota:** la creación rápida envía principalmente nombre, lista y proyecto. No des por hecho que se están guardando otros campos de tarea que no se capturan en ese formulario.

### F-08 — Detalle y actualización de una tarea

1. Pulsa una tarea del tablero.
2. Comprueba que se abra el modal y que muestre sus datos existentes.
3. Modifica la descripción, prioridad, fecha de vencimiento o lista.
4. Pulsa **Guardar cambios**.
5. Vuelve a abrir la tarea o recarga el tablero; confirma qué cambios se conservaron y si la tarea aparece en la lista seleccionada.

**Resultado esperado:** al volver a consultar, la tarea debe reflejar los cambios guardados. Si no cambia o aparece una tarea duplicada, anótalo como defecto: el formulario del modal envía a la ruta de guardado de tarea y debe comprobarse contra la base de datos.

### F-09 — Comentarios de tarea

1. Abre el detalle de una tarea.
2. Escribe un comentario y usa el botón de envío.
3. Espera la respuesta y vuelve a abrir el detalle o recarga el tablero.
4. Comprueba que el comentario y la fecha aparezcan en la tarea correcta.

**Resultado esperado:** el comentario queda guardado y se muestra en el detalle correspondiente. Si no aparece, revisa la consola del navegador y los logs de ambos servicios; anota cualquier error de formato de fecha o de comunicación.

### F-10 — Eliminación de tarea

1. En una tarea de prueba, pulsa el icono de eliminación.
2. Comprueba que aparezca la confirmación con el nombre de la tarea.
3. Pulsa **Cancelar** y confirma que la tarea siga ahí.
4. Repite y pulsa **Aceptar**.
5. Comprueba que el tablero se actualice y que la tarea ya no aparezca.

**Resultado esperado:** cancelar no modifica los datos; aceptar elimina la tarea seleccionada.

### F-11 — Edición de proyecto

1. Regresa a `/inicio` y abre el menú de opciones de una tarjeta de prueba.
2. Selecciona **Editar**.
3. Comprueba que el formulario cargue los datos actuales del proyecto.
4. Cambia un dato descriptivo y guarda.
5. Confirma que la tarjeta muestre el cambio después de regresar a `/inicio` o recargar la página.

**Resultado esperado:** se recuperan los datos del proyecto, se guarda la edición y se refleja en la tarjeta.

### F-12 — Eliminación de proyecto

1. En la tarjeta de un proyecto de prueba, abre el menú y selecciona **Eliminar**.
2. Comprueba que la confirmación muestre el nombre del proyecto.
3. Pulsa **Cancelar** y confirma que el proyecto permanezca.
4. Repite y confirma la eliminación con **Aceptar**.
5. Comprueba que desaparezca de `/inicio`.

**Resultado esperado:** cancelar conserva el proyecto; confirmar lo elimina. No uses proyectos con información que debas conservar.

### F-13 — Manejo de indisponibilidad de la API

1. Deja cargada la pantalla de inicio o proyecto.
2. Detén la API temporalmente sin detener la aplicación web.
3. Recarga la pantalla o repite una acción que consulte o guarde datos.
4. Observa el mensaje de la página, la consola del navegador y la terminal del front.
5. Vuelve a iniciar la API y confirma que la aplicación pueda volver a cargar datos.

**Resultado esperado:** registra si la interfaz informa claramente del fallo y si vuelve a funcionar al restaurar la API. La aplicación actual puede limitarse a mostrar un error en la consola; no se garantiza un mensaje visible para el usuario.

## 4. Pruebas visuales y de usabilidad

Para las pantallas de inicio, registro, login, inicio de proyectos y tablero:

- Revisa los anchos de escritorio y móvil: que formularios, tarjetas, modales y botones no se corten ni se superpongan.
- Comprueba que los botones, enlaces, menús y modales respondan al ratón y al teclado.
- Revisa que las imágenes de fondo, el logotipo, iconos y estilos carguen.
- Usa el zoom del navegador (por ejemplo, 125 % y 200 %) para detectar texto oculto o controles inaccesibles.
- Comprueba contraste y legibilidad de textos sobre fondos e imágenes.
- Abre las herramientas de desarrollador y revisa errores JavaScript, solicitudes fallidas y respuestas HTTP inesperadas.

## 5. Limitaciones conocidas al interpretar resultados

- El front consume la API en `http://localhost:8081`; una prueba de interfaz que requiera datos no es independiente del backend.
- El usuario autenticado se conserva en `localStorage`; borrar los datos del sitio elimina ese estado local.
- La validación de acceso se basa en la respuesta del endpoint de login; no equivale a una sesión protegida del lado del servidor.
- La edición de proyectos y tareas debe confirmarse después de guardar y recargar. Si el registro no cambia o se duplica, registra la ruta utilizada y los datos resultantes.
- Al guardar un comentario, la interfaz genera la fecha en formato `yy/MM/dd HH:mm`. Si la API espera otro formato, el guardado puede fallar.
- No se garantiza una notificación visual uniforme ante todos los errores de red; consulta la consola del navegador y las terminales.

## 6. Registro de resultados

| ID | Caso | Resultado (Aprobado/Fallido/No ejecutado) | Evidencia o comentario |
|---|---|---|---|
| F-01 | Navegación y recursos | | |
| F-02 | Validaciones del login | | |
| F-03 | Login inválido y válido | | |
| F-04 | Registro de usuario | | |
| F-05 | Ventanas informativas | | |
| F-06 | Alta y visualización de proyecto | | |
| F-07 | Tablero y listas de tareas | | |
| F-08 | Detalle y actualización de tarea | | |
| F-09 | Comentarios | | |
| F-10 | Eliminación de tarea | | |
| F-11 | Edición de proyecto | | |
| F-12 | Eliminación de proyecto | | |
| F-13 | API no disponible | | |
