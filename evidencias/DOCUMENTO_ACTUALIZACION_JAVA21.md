# Documento Técnico de Actualización a Java 21 y Configuración de Jenkins

## 1. Resumen Ejecutivo
Este documento describe el proceso técnico y los cambios implementados para la actualización de los dos microservicios del proyecto hacia **Java 21 (LTS)**, así como la configuración del entorno de integración continua en **Jenkins** para soportar compilaciones con **JDK 21** y **Maven compatible**.

---

## 2. Microservicios del Proyecto

| # | Microservicio | Tipo / Rol | Framework | Versión Previa | Versión Actualizada |
|---|---------------|------------|-----------|----------------|---------------------|
| 1 | **Ejercicio5Cliente** | Frontend (Thymeleaf + Web) | Spring Boot 3.2.5 | Java 17 | **Java 21** |
| 2 | **Ejercicio5MyAPIGestor** | Backend (API REST + JPA + MariaDB) | Spring Boot 3.2.5 | Java 17 | **Java 21** |

---

## 3. Detalle de Cambios en los Archivos `pom.xml`

### 3.1. Microservicio Frontend: `Ejercicio5Cliente/pom.xml`
* **Línea modificada:**
  ```xml
  <!-- ANTES (Estado Original) -->
  <properties>
      <java.version>17</java.version>
  </properties>

  <!-- AHORA (Estado Actualizado) -->
  <properties>
      <java.version>21</java.version>
  </properties>
  ```
* **Compatibilidad de Framework y Plugins:**
  * El proyecto utiliza `spring-boot-starter-parent` en versión **3.2.5**, la cual incluye soporte nativo y oficial para Java 21.
  * El plugin `spring-boot-maven-plugin` hereda la compatibilidad de JDK 21 gestionada por el padre.
  * Se configuró el Maven Wrapper (`.mvn/wrapper`) con Maven 3.9.9 para asegurar portabilidad y compilación determinista.

---

### 3.2. Microservicio Backend: `Ejercicio5MyAPIGestor/pom.xml`
* **Línea modificada:**
  ```xml
  <!-- ANTES (Estado Original) -->
  <properties>
      <java.version>17</java.version>
  </properties>

  <!-- AHORA (Estado Actualizado) -->
  <properties>
      <java.version>21</java.version>
  </properties>
  ```
* **Compatibilidad de Framework y Dependencias:**
  * El backend corre sobre Spring Boot 3.2.5 con dependencias para Spring Data JPA, Hibernate, MariaDB JDBC driver y H2 database para pruebas.
  * Todas las librerías son compatibles sin requerir parches adicionales de bytecode en Java 21.
  * La compilación utiliza `maven-compiler-plugin:3.11.0` gestionado automáticamente por Spring Boot, el cual cuenta con compatibilidad total con Java 21.

---

## 4. Configuración del Entorno Jenkins

### 4.1. Versión de Jenkins y Runtime JDK 21
* **Contenedor:** Docker ejecutando la imagen oficial `jenkins/jenkins:lts-jdk21`.
* **JDK del Sistema Jenkins:**
  ```text
  openjdk version "21.0.12.1" 2026-08-18 LTS
  OpenJDK Runtime Environment Temurin-21.0.12.1+1 (build 21.0.12.1+1-LTS)
  OpenJDK 64-Bit Server VM Temurin-21.0.12.1+1 (build 21.0.12.1+1-LTS, mixed mode)
  Ruta (JAVA_HOME): /opt/java/openjdk
  ```

### 4.2. Configuración de Herramientas Globales (Global Tool Configuration)
En el panel de administración de Jenkins (**Administrar Jenkins > Tools / Herramientas**), se configuraron las siguientes herramientas:

1. **JDK 21 (`JDK21`)**:
   * **Nombre:** `JDK21`
   * **JAVA_HOME:** `/opt/java/openjdk`
   * Cumple con la ejecución de pipelines y tareas de compilación usando Java 21 LTS.

2. **Maven (`Maven21`)**:
   * **Nombre:** `Maven21`
   * **Versión instalada:** `3.10.0` (o `3.9.x`)
   * **Requisito técnico:** Java 21 requiere Maven >= 3.8.8 para compilar correctamente sin advertencias de ASM/bytecode; la versión 3.10.0 / 3.9.x supera este estándar y garantiza compatibilidad plena con el `maven-compiler-plugin`.

---

## 5. Pruebas y Validación de Compilación

Ambos microservicios fueron compilados exitosamente bajo el entorno de Java 21:

1. **`Ejercicio5Cliente`**:
   ```text
   [INFO] Building Ejercicio5Cliente 0.0.1-SNAPSHOT
   [INFO] --- compiler:3.11.0:compile (default-compile) @ Ejercicio5Cliente ---
   [INFO] BUILD SUCCESS
   ```

2. **`Ejercicio5MyAPIGestor`**:
   ```text
   [INFO] Building Ejercicio5MyAPIGestor 0.0.1-SNAPSHOT
   [INFO] --- compiler:3.11.0:compile (default-compile) @ Ejercicio5MyAPIGestor ---
   [INFO] BUILD SUCCESS
   ```

---

## 6. Inventario de Evidencias Entregadas

### 6.1. Archivos POM
* **Originales (Java 17):**
  * `evidencias/originales/pom-Cliente-original.xml`
  * `evidencias/originales/pom-MyAPIGestor-original.xml`
* **Actualizados (Java 21):**
  * `evidencias/actualizados/pom-Cliente-actualizado.xml`
  * `evidencias/actualizados/pom-MyAPIGestor-actualizado.xml`

### 6.2. Ligas de GitHub
* **Repositorio principal:**  
  https://github.com/Gestion-Proceso-Desarrollo-Software/admin-proyecto
* **Archivo `pom.xml` actualizado - Microservicio Cliente:**  
  https://github.com/Gestion-Proceso-Desarrollo-Software/admin-proyecto/blob/main/Ejercicio5Cliente/pom.xml
* **Archivo `pom.xml` actualizado - Microservicio Backend (API Gestor):**  
  https://github.com/Gestion-Proceso-Desarrollo-Software/admin-proyecto/blob/main/Ejercicio5MyAPIGestor/pom.xml
* **Carpeta de evidencias:**  
  https://github.com/Gestion-Proceso-Desarrollo-Software/admin-proyecto/tree/main/evidencias
