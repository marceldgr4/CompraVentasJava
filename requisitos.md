# 📄 Especificación de Requisitos — Sistema CompraVenta

> **Proyecto:** CompraVenta (Java Desktop + Supabase PostgreSQL)
> **Versión del documento:** 1.0 — 21 de abril de 2026

---

## 1. Descripción General

El sistema **CompraVenta** es una aplicación de escritorio desarrollada en Java (Swing) que permite gestionar operaciones de **compra, venta y empeño de artículos** de segunda mano. Los datos se persisten en una base de datos **Supabase (PostgreSQL)** y la autenticación se realiza mediante la API REST de Supabase Auth.

---

## 2. Stakeholders y Roles

| Rol | Descripción |
|-----|-------------|
| **Administrador (Admin)** | Tiene acceso completo al sistema: gestión de empleados, artículos, clientes, ventas y empeños. |
| **Empleado** | Puede registrar empeños, consultar artículos y clientes, pero no puede eliminar ni modificar datos críticos. |

---

## 3. Requisitos Funcionales

### RF-01 — Módulo de Autenticación

| ID | Requisito |
|----|-----------|
| RF-01.1 | El sistema debe permitir al usuario iniciar sesión con correo y contraseña mediante la API de Supabase Auth. |
| RF-01.2 | Al iniciar sesión, el sistema debe cargar el perfil del usuario (nombre completo, rol, estado activo) desde la tabla `profile`. |
| RF-01.3 | Si el perfil está marcado como inactivo (`active = false`), el sistema debe denegar el acceso y mostrar un mensaje al usuario. |
| RF-01.4 | El sistema debe permitir al Administrador registrar nuevos empleados con correo, contraseña y nombre completo. |
| RF-01.5 | El sistema debe permitir cerrar sesión, invalidando el token en Supabase. |
| RF-01.6 | La sesión activa debe persistirse en memoria durante la ejecución (no en disco) usando `SessionManager`. |

---

### RF-02 — Módulo de Clientes

| ID | Requisito |
|----|-----------|
| RF-02.1 | El sistema debe listar todos los clientes ordenados por apellido y nombre. |
| RF-02.2 | El sistema debe permitir buscar clientes por nombre, apellido o correo electrónico. |
| RF-02.3 | El sistema debe permitir registrar nuevos clientes con: nombre, apellido, correo y teléfono. |
| RF-02.4 | Solo el Administrador puede modificar los datos de un cliente. |
| RF-02.5 | Solo el Administrador puede eliminar un cliente. |
| RF-02.6 | El sistema debe validar que el correo y teléfono tengan formato válido antes de guardar. |
| RF-02.7 | El sistema debe impedir eliminar un cliente que tenga ventas o empeños activos asociados. |

---

### RF-03 — Módulo de Artículos (Inventario)

| ID | Requisito |
|----|-----------|
| RF-03.1 | El sistema debe listar todos los artículos del inventario, ordenados por nombre. |
| RF-03.2 | El sistema debe permitir buscar artículos por nombre (búsqueda parcial). |
| RF-03.3 | El sistema debe mostrar el estado de cada artículo: disponible (stock > 0), vendido, etc. |
| RF-03.4 | Solo el Administrador puede registrar nuevos artículos con: nombre, descripción, precio, cantidad y cliente propietario. |
| RF-03.5 | Solo el Administrador puede editar nombre, precio y tipo de un artículo. |
| RF-03.6 | Solo el Administrador puede eliminar un artículo. |
| RF-03.7 | El sistema debe permitir aumentar el stock de un artículo (`addStock`). |
| RF-03.8 | El sistema debe reducir el stock automáticamente al registrar una venta o empeño (`removeStock`). |
| RF-03.9 | El sistema debe validar que el precio sea mayor a 0 y la cantidad no sea negativa. |

---

### RF-04 — Módulo de Empeños (Pawns)

| ID | Requisito |
|----|-----------|
| RF-04.1 | El sistema debe listar todos los empeños con datos del empleado, artículo y cliente. |
| RF-04.2 | El Administrador ve todos los empeños; el Empleado solo los suyos. |
| RF-04.3 | El sistema debe listar empeños activos (no devueltos ni expirados). |
| RF-04.4 | El sistema debe listar empeños vencidos (pasaron su fecha límite). |
| RF-04.5 | El sistema debe permitir registrar un nuevo empeño con: artículo, cliente, cantidad, precio, fecha de empeño y fecha de devolución. |
| RF-04.6 | La fecha de devolución debe ser posterior a la fecha de empeño. |
| RF-04.7 | El sistema debe permitir marcar un empeño como **devuelto**. |
| RF-04.8 | Solo el Administrador puede marcar un empeño como **expirado** manualmente. |
| RF-04.9 | El sistema debe procesar automáticamente todos los empeños vencidos al iniciarse (o bajo demanda del Admin). |
| RF-04.10 | Solo el Administrador puede eliminar un empeño. |
| RF-04.11 | El sistema debe calcular el valor total de todos los empeños activos. |

---

### RF-05 — Módulo de Ventas (pendiente de implementar)

| ID | Requisito |
|----|-----------|
| RF-05.1 | El sistema debe permitir registrar una venta con: empleado, cliente, fecha y lista de artículos (con cantidad y precio). |
| RF-05.2 | Al registrar una venta, el sistema debe reducir el stock de cada artículo vendido. |
| RF-05.3 | Si el stock de algún artículo es insuficiente, la venta no debe procesarse (debe ser atómica/transaccional). |
| RF-05.4 | El sistema debe listar ventas por fecha, por cliente o por empleado. |
| RF-05.5 | El sistema debe mostrar el total calculado de una venta. |
| RF-05.6 | Solo el Administrador puede eliminar una venta registrada. |

---

### RF-06 — Módulo de Perfil / Empleados

| ID | Requisito |
|----|-----------|
| RF-06.1 | El sistema debe listar todos los perfiles de empleados. |
| RF-06.2 | Solo el Administrador puede activar o desactivar un perfil de empleado. |
| RF-06.3 | Un empleado desactivado no puede iniciar sesión. |

---

### RF-07 — Dashboard

| ID | Requisito |
|----|-----------|
| RF-07.1 | El dashboard debe mostrar el total de artículos en inventario. |
| RF-07.2 | El dashboard debe mostrar el número de empeños activos. |
| RF-07.3 | El dashboard debe mostrar el valor total de empeños activos. |
| RF-07.4 | El dashboard debe mostrar el número de empeños vencidos. |
| RF-07.5 | El dashboard debe mostrar el número de clientes registrados. |

---

## 4. Requisitos No Funcionales

### RNF-01 — Seguridad

| ID | Requisito |
|----|-----------|
| RNF-01.1 | Las contraseñas deben manejarse exclusivamente por Supabase Auth (nunca almacenadas en texto plano localmente). |
| RNF-01.2 | Los tokens de acceso deben almacenarse solo en memoria (nunca en archivos). |
| RNF-01.3 | Cada operación sensible debe verificar el rol del usuario antes de ejecutarse. |
| RNF-01.4 | Las cadenas de conexión y claves API no deben incluirse directamente en el código fuente; usar `.env`. |

### RNF-02 — Rendimiento

| ID | Requisito |
|----|-----------|
| RNF-02.1 | Las operaciones de base de datos deben ejecutarse fuera del EDT de Swing usando `AsyncTaskExecutor` o `SwingWorker`. |
| RNF-02.2 | El pool de conexiones (HikariCP) debe configurarse con un máximo de conexiones apropiado para el entorno. |
| RNF-02.3 | Las consultas frecuentes deben usar `PreparedStatement` con caché de sentencias habilitado. |

### RNF-03 — Usabilidad

| ID | Requisito |
|----|-----------|
| RNF-03.1 | Los mensajes de error deben ser comprensibles por el usuario (no mostrar stack traces). |
| RNF-03.2 | Todas las operaciones largas deben mostrar un indicador de carga al usuario. |
| RNF-03.3 | Los formularios deben validar la entrada antes de enviar la solicitud al servicio. |

### RNF-04 — Mantenibilidad

| ID | Requisito |
|----|-----------|
| RNF-04.1 | El código debe seguir la arquitectura en capas: UI → ViewModel → Service → DAO. |
| RNF-04.2 | Las excepciones de infraestructura (SQLException) no deben propagarse a la capa UI; deben transformarse en `ServiceException`. |
| RNF-04.3 | Cada clase debe tener una única responsabilidad (principio SRP). |
| RNF-04.4 | Las clases utilitarias deben ser `final` con constructor privado. |

### RNF-05 — Disponibilidad y Conectividad

| ID | Requisito |
|----|-----------|
| RNF-05.1 | El sistema debe manejar errores de red al conectarse a Supabase y mostrar un mensaje apropiado. |
| RNF-05.2 | La aplicación debe liberarse correctamente al cerrarse (cerrar pool de conexiones con `ConnectionPool.close()`). |

---

## 5. Restricciones del Sistema

- **Plataforma:** Java 21, ejecución local como aplicación Swing.
- **Base de datos:** Supabase (PostgreSQL) — acceso remoto vía JDBC y API REST.
- **Autenticación:** Solo mediante Supabase Auth (no se implementa auth propio).
- **Sin ORM:** Acceso a datos manual mediante `PreparedStatement` (sin Hibernate/JPA).
