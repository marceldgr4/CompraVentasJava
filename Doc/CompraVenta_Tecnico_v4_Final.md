**Documento Técnico v4.0**

## **SISTEMA COMPRAVENTA**


DOCUMENTO TÉCNICO DE INGENIERÍA DE REQUISITOS,

MODELADO DE DATOS Y BACKLOG DE PRODUCTO

|Versión|4.0 — Definitivo|
|---|---|
|**Fecha**|Abril 2026|
|**Stack**|Java 21 · Swing · Supabase (PostgreSQL 15)|
|**Metodología**|Scrum · Historias de Usuario (Conectar)|
|**Clasificación**|Confidencial — Solo equipo de desarrollo|
|**Estándar**|IEEE 830 · Google Java Style Guide · 3FN|
|**Arquitectura**|MVP · DAO · Repository · Strategy · Singleton|



1. Resumen Ejecutivo y Contexto del Sistema

## **1.1 Descripción General**


CompraVenta es una aplicación de escritorio (Java 21 + Swing) orientada a la gestión integral de operaciones de
compra, venta y empeño de artículos de segunda mano. Persiste su información en Supabase (PostgreSQL 15)
y delega la autenticación a Supabase Auth. El sistema soporta dos roles: Administrador y Empleado, con
permisos diferenciados implementados mediante Row Level Security en base de datos y validación en capa de
servicio.

Este documento consolida, corrige y amplía la especificación original v2.0, resolviendo todas las ambigüedades,
contradicciones y omisiones detectadas en el análisis de revisión. El resultado es un documento técnico listo
para implementación directa.

## **1.2 Análisis de Calidad del Documento Fuente (v2.0)**

|Dimensión|Evaluación|Hallazgo Principal|
|---|---|---|
|Consistencia<br>funcional|DEFICIENTE|Contradicciones entre HUs y RFs en permisos de Empleado|
|Claridad de requisitos|REGULAR|HU-12 (empeños) incompleta: cuotas definidas en narrativa<br>pero ausentes del DDL|
|Cobertura del<br>negocio|REGULAR|Sin módulo de facturación PDF, sin tabla de pagos de<br>cuotas|
|Calidad del modelo<br>de datos|DEFICIENTE|pawns sin estados enumerados, sin cuotas, sin peso.<br>clientes sin soft-delete|
|Trazabilidad|BUENA|Matriz HU↔RF presente aunque con referencias a RFs<br>inexistentes|
|RNFs|BUENA|Bien estructurados con métricas medibles|



Compra Venta © Abril 2026  |  Página 1


**Documento Técnico v4.0**

## **1.3 Problemas Detectados — Registro Formal**




















|ID|Problema Detectado|Sección|Severidad|Resolución Aplicada|
|---|---|---|---|---|
|**P-01**|HU-12 menciona cuotas<br>(installment_count,<br>installments_paid) pero el DDL no<br>tiene esas columnas en pawns.|HU-12 / DDL|**CRÍTICA**|Se añaden columnas<br>installment_count,<br>installments_paid,<br>installments_missed y<br>weight_grams al DDL.|
|**P-02**|Los 6 estados de empeño (activo,<br>vencido, finalizado, perdido,<br>retirado, vendido) son imposibles de<br>representar con expired BOOLEAN<br>y returned BOOLEAN.|HU-12/13 /<br>DDL|**CRÍTICA**|Se reemplaza por ENUM<br>pawn_status con 6 valores y<br>lógica de transición de estados.|
|**P-03**|RF-02.6 dice "Solo Admin puede<br>crear clientes" pero HU-05 dice "Rol:<br>Administrador, empleados".<br>Contradicción directa.|RF-02.6 /<br>HU-05|**ALTA**|Empleado PUEDE crear y leer<br>clientes. Solo Admin puede<br>editar y eliminar<br>permanentemente.|
|**P-04**|RF-03.4 dice "Solo Admin puede<br>crear artículos" pero HU-09 dice<br>"Rol: Administrador, empleado".|RF-03.4 /<br>HU-09|**ALTA**|Empleado PUEDE registrar<br>artículos. Precio solo editable<br>por Admin (o con autorización).|
|**P-05**|HU-07 describe soft-delete para<br>empleados pero la tabla clientes no<br>tiene columna status en el DDL<br>original.|HU-07 / DDL|**ALTA**|Se añade status ENUM<br>(Activo/Eliminado) a la tabla<br>clientes.|
|**P-06**|pawns no tiene columna<br>weight_grams pero HU-12 exige<br>peso para joyería.|HU-12 / DDL|**MEDIA**|Se añade weight_grams<br>NUMERIC(8,2) nullable con<br>validación por trigger.|
|**P-07**|RLS delegado a capa servicio y a<br>Supabase simultáneamente sin<br>definir responsabilidades claras.|RF-04.1 /<br>RNF-01.5|**MEDIA**|RLS en BD como primera<br>línea; capa servicio como<br>segunda (defensa en<br>profundidad).|
|**P-08**|Sin módulo de facturación PDF pese<br>a ser un negocio con ventas y<br>empeños.|Global|**MEDIA**|Se diseña HU-22 y módulo<br>completo con iText 7 / Apache<br>PDFBox.|
|**P-09**|v_dashboard referenciada en<br>RF-07.1 pero sin DDL definido.|RF-07.1|**MEDIA**|Se incluye DDL completo de la<br>vista en el script SQL.|
|**P-10**|fn_expire_overdue_pawns()<br>referenciada en RF-04.6 sin<br>implementación SQL.|RF-04.6|**MEDIA**|Se incluye implementación<br>completa con audit_log en el<br>script SQL.|


## **1.4 Tabla de Permisos Consolidada — Resolución Definitiva**

|Operación|Administrador|Empleado|
|---|---|---|
|Login / Logout|✓|✓|
|Ver dashboard|Completo<br>✓|(sin métricas globales de<br>✓<br>empleados)|
|Registrar cliente|✓|✓|



Compra Venta © Abril 2026  |  Página 2


**Documento Técnico v4.0**

|Operación|Administrador|Empleado|
|---|---|---|
|Editar cliente|✓|✗|
|Eliminar cliente (permanente)|✓|✗|
|Soft-delete cliente|✓|(solo marca status = Eliminado)<br>✓|
|Ver inventario|✓|✓|
|Registrar artículo|✓|✓|
|Editar artículo (todos los campos)|✓|✗|
|Editar artículo (excepto precio)|✓|✓|
|Editar precio de artículo|Directo<br>✓|Con autorización temporal del<br>✓<br>Admin|
|Ajuste de stock|✓|✓|
|Registrar empeño|✓|✓|
|Ver empeños propios|✓|✓|
|Consultar empeños de compañeros|✓|Solo lectura (datos básicos)<br>✓|
|Marcar empeño devuelto (propio)|✓|✓|
|Marcar empeño expirado (manual)|✓|✗|
|Eliminar empeño|✓|✗|
|Registrar pago de cuota|✓|✓|
|Registrar venta|✓|✓|
|Consultar historial ventas|Todos<br>✓|Solo las propias<br>✓|
|Eliminar venta|✓|✗|
|Gestionar empleados (activar/desactivar)|✓|✗|
|Generar factura PDF|✓|✓|



Compra Venta © Abril 2026  |  Página 3


**Documento Técnico v4.0**

# **2. Backlog de Producto — Historias de Usuario Completas**


Todas las historias siguen el formato Conectar: Como [rol] / Quiero [acción] / Para [beneficio]. Los criterios de
aceptación usan BDD: Dado/Cuando/Entonces. ✓ = obligatorio · = condicional.△

## **HU-1 — Módulo de Autenticación**

|HU-01 — Iniciar sesión con correo institucional|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 1|5 pts|**Alta**|
|**Rol**|Usuario del sistema (Admin o Empleado)|Usuario del sistema (Admin o Empleado)|Usuario del sistema (Admin o Empleado)|Usuario del sistema (Admin o Empleado)|
|**Como...**|Quiero iniciar sesión con mi correo y contraseña|Quiero iniciar sesión con mi correo y contraseña|Quiero iniciar sesión con mi correo y contraseña|Quiero iniciar sesión con mi correo y contraseña|
|**Para...**|acceder de forma segura a las funciones del sistema según mi rol asignado|acceder de forma segura a las funciones del sistema según mi rol asignado|acceder de forma segura a las funciones del sistema según mi rol asignado|acceder de forma segura a las funciones del sistema según mi rol asignado|



**Reglas de Negocio**

   - El sistema nunca almacena contraseñas localmente. El token JWT se guarda únicamente en memoria
(SessionManager en RAM).

   - SessionManager es un Singleton thread-safe (volatile + double-checked locking).

   - Ninguna credencial se escribe en disco, log ni portapapeles.


**Criterios de Aceptación**

|✓|Dado que tengo credenciales válidas, cuando las ingreso, entonces el sistema me autentica, carga mi perfil<br>(nombre, rol) y muestra el dashboard correspondiente en menos de 3 segundos.|
|---|---|
|**✓ **|Dado que la cuenta está desactivada (active = false), cuando intento iniciar sesión, entonces el sistema<br>muestra "Tu cuenta está desactivada. Contacta al administrador." y no permite el acceso.|
|**✓ **|Dado que ingreso una contraseña incorrecta, cuando intento iniciar sesión, entonces el sistema muestra<br>"Credenciales incorrectas" sin revelar si el error es en correo o contraseña.|
|**✓ **|Dado que el servidor Supabase no responde, cuando intento iniciar sesión, entonces el sistema muestra "No<br>se pudo conectar con el servidor. Verifica tu conexión." sin mostrar stack trace.|
|**✓ **|La sesión activa se mantiene en memoria durante la ejecución y no se persiste en disco en ningún formato.|


|HU-02 — Registrar nuevos empleados|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 1|5 pts|**Alta**|
|**Rol**|Administrador|Administrador|Administrador|Administrador|
|**Como...**|Quiero registrar nuevos empleados con nombre completo, correo, contraseña<br>temporal y rol|Quiero registrar nuevos empleados con nombre completo, correo, contraseña<br>temporal y rol|Quiero registrar nuevos empleados con nombre completo, correo, contraseña<br>temporal y rol|Quiero registrar nuevos empleados con nombre completo, correo, contraseña<br>temporal y rol|
|**Para...**|incorporar al equipo al sistema de manera controlada y con el nivel de acceso<br>correcto desde el primer día|incorporar al equipo al sistema de manera controlada y con el nivel de acceso<br>correcto desde el primer día|incorporar al equipo al sistema de manera controlada y con el nivel de acceso<br>correcto desde el primer día|incorporar al equipo al sistema de manera controlada y con el nivel de acceso<br>correcto desde el primer día|



**Reglas de Negocio**


Compra Venta © Abril 2026  |  Página 4


**Documento Técnico v4.0**


   - El rol se envía en raw_user_meta_data.rol para que el trigger on_auth_user_created lo persista en
profile.

   - La contraseña temporal debe cumplir mínimo 8 caracteres, al menos una mayúscula y un número
(validado en UI).


**Criterios de Aceptación**







|✓|Dado que completo el formulario con datos válidos y selecciono el rol, cuando presiono "Registrar", entonces<br>el sistema crea el usuario en Supabase Auth con metadata {full_name, rol} y el perfil aparece en la lista de<br>empleados.|
|---|---|
|**✓ **|Dado que intento registrar un correo ya existente, cuando presiono "Registrar", entonces el sistema muestra<br>"El correo ya está registrado en el sistema."|
|**✓ **|Dado que dejo campos obligatorios vacíos, cuando presiono "Registrar", entonces el formulario resalta los<br>campos faltantes antes de enviar la petición.|
|**△ **|El Admin puede seleccionar el rol (Admin o Empleado) al momento del registro.|


|HU-03 — Cerrar sesión desde cualquier pantalla|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 1|2 pts|**Alta**|
|**Rol**|Usuario autenticado (Admin o Empleado)|Usuario autenticado (Admin o Empleado)|Usuario autenticado (Admin o Empleado)|Usuario autenticado (Admin o Empleado)|
|**Como...**|Quiero cerrar sesión desde cualquier pantalla del sistema|Quiero cerrar sesión desde cualquier pantalla del sistema|Quiero cerrar sesión desde cualquier pantalla del sistema|Quiero cerrar sesión desde cualquier pantalla del sistema|
|**Para...**|proteger mi cuenta al terminar mi turno o al alejarme del equipo|proteger mi cuenta al terminar mi turno o al alejarme del equipo|proteger mi cuenta al terminar mi turno o al alejarme del equipo|proteger mi cuenta al terminar mi turno o al alejarme del equipo|


**Criterios de Aceptación**

|✓|Dado que presiono "Cerrar sesión", cuando confirmo la acción, entonces el sistema invalida el token en<br>Supabase, limpia el SessionManager y regresa a la pantalla de login.|
|---|---|
|**✓ **|Después del logout, presionar el botón Atrás no permite volver a pantallas autenticadas.|
|**✓ **|El proceso de logout completa en menos de 2 segundos.|


## **HU-2 — Módulo de Gestión de Clientes**

|HU-04 — Ver listado completo de clientes|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 1|3 pts|**Alta**|
|**Rol**|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|
|**Como...**|Quiero ver el listado completo de clientes ordenado por apellido y poder buscar<br>por nombre, apellido o correo|Quiero ver el listado completo de clientes ordenado por apellido y poder buscar<br>por nombre, apellido o correo|Quiero ver el listado completo de clientes ordenado por apellido y poder buscar<br>por nombre, apellido o correo|Quiero ver el listado completo de clientes ordenado por apellido y poder buscar<br>por nombre, apellido o correo|
|**Para...**|localizar rápidamente a un cliente al momento de registrar una operación|localizar rápidamente a un cliente al momento de registrar una operación|localizar rápidamente a un cliente al momento de registrar una operación|localizar rápidamente a un cliente al momento de registrar una operación|



**Reglas de Negocio**


Compra Venta © Abril 2026  |  Página 5


**Documento Técnico v4.0**


   - La búsqueda usa índices GIN (pg_trgm) para coincidencia parcial case-insensitive con debounce de
300ms en UI.


**Criterios de Aceptación**

|✓|El listado muestra: apellido, nombre, correo y teléfono, ordenado alfabéticamente por apellido ASC, nombre<br>ASC.|
|---|---|
|**✓ **|Dado que escribo al menos 2 caracteres en el buscador, cuando presiono buscar o Enter, entonces el<br>sistema filtra resultados en tiempo real (debounce 300ms).|
|**✓ **|Si no hay resultados, se muestra "No se encontraron clientes con ese criterio."|
|**✓ **|El listado se carga en hilo secundario (SwingWorker) mostrando un spinner mientras carga.|
|**✓ **|Los empleados ven solo clientes con status = Activo. El Admin puede filtrar también por status = Eliminado.|



**Dependencias:** HU-01 (autenticación requerida)

|HU-05 — Registrar nuevo cliente|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 1|3 pts|**Alta**|
|**Rol**|Administrador o Empleado|Administrador o Empleado|Administrador o Empleado|Administrador o Empleado|
|**Como...**|Quiero registrar un nuevo cliente con nombre, apellido, correo electrónico y<br>teléfono|Quiero registrar un nuevo cliente con nombre, apellido, correo electrónico y<br>teléfono|Quiero registrar un nuevo cliente con nombre, apellido, correo electrónico y<br>teléfono|Quiero registrar un nuevo cliente con nombre, apellido, correo electrónico y<br>teléfono|
|**Para...**|tener un registro centralizado de los clientes con datos de contacto válidos|tener un registro centralizado de los clientes con datos de contacto válidos|tener un registro centralizado de los clientes con datos de contacto válidos|tener un registro centralizado de los clientes con datos de contacto válidos|



**Reglas de Negocio**

   - El correo se valida con expresión regular RFC-5321 antes de enviarse al servicio.

   - El teléfono acepta solo dígitos, "+" y entre 7-15 caracteres (CHECK en BD + validación UI).


**Criterios de Aceptación**

|✓|Dado que completo todos los campos con datos válidos, cuando presiono "Guardar", entonces el cliente se<br>persiste con status = Activo y aparece en el listado.|
|---|---|
|**✓ **|El correo se valida con formato estándar (regex RFC-5321).|
|**✓ **|Dado que el correo ya existe en la base de datos, entonces el sistema muestra "Ya existe un cliente<br>registrado con ese correo."|
|**✓ **|Los campos Nombre, Apellido, Correo y Teléfono son obligatorios; el formulario impide el envío si están<br>vacíos.|


|HU-06 — Editar datos de cliente existente|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 2|3 pts|**Media**|
|**Rol**|Administrador|Administrador|Administrador|Administrador|
|**Como...**|Quiero editar los datos de un cliente existente|Quiero editar los datos de un cliente existente|Quiero editar los datos de un cliente existente|Quiero editar los datos de un cliente existente|



Compra Venta © Abril 2026  |  Página 6


**Documento Técnico v4.0**


**Reglas de Negocio**

   - Solo el Administrador puede editar clientes (P-03 resuelto).


**Criterios de Aceptación**

|✓|Dado que selecciono un cliente y presiono "Editar", el formulario se precarga con sus datos actuales.|
|---|---|
|**✓ **|Al guardar los cambios, el sistema actualiza únicamente los campos modificados y registra updated_at.|
|**✓ **|Si el nuevo correo ya pertenece a otro cliente, el sistema lo informa y no guarda.|
|**✓ **|Las mismas validaciones de creación (correo, teléfono) aplican en la edición.|



**Dependencias:** HU-05

|HU-07 — Eliminar o marcar cliente como eliminado|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 2|3 pts|**Media**|
|**Rol**|Administrador (eliminación permanente) / Empleado (soft-delete)|Administrador (eliminación permanente) / Empleado (soft-delete)|Administrador (eliminación permanente) / Empleado (soft-delete)|Administrador (eliminación permanente) / Empleado (soft-delete)|
|**Como...**|Quiero eliminar un cliente del sistema de forma segura|Quiero eliminar un cliente del sistema de forma segura|Quiero eliminar un cliente del sistema de forma segura|Quiero eliminar un cliente del sistema de forma segura|
|**Para...**|mantener la base de datos limpia sin registros obsoletos, siempre que no haya<br>operaciones activas vinculadas|mantener la base de datos limpia sin registros obsoletos, siempre que no haya<br>operaciones activas vinculadas|mantener la base de datos limpia sin registros obsoletos, siempre que no haya<br>operaciones activas vinculadas|mantener la base de datos limpia sin registros obsoletos, siempre que no haya<br>operaciones activas vinculadas|



**Reglas de Negocio**

   - La restricción ON DELETE RESTRICT en articles.cliente_id, pawns.cliente_id y sales.cliente_id se valida
antes del DELETE físico.

   - Solo el Administrador puede eliminar clientes de forma permanente (DELETE físico).

   - El Empleado realiza un soft-delete: cambia status de "Activo" a "Eliminado". El cliente desaparece de sus
listados pero el Admin lo ve con el filtro correspondiente.

   - El botón "Eliminar" permanente solo aparece en la interfaz del Admin.


**Criterios de Aceptación**

|✓|Dado que selecciono un cliente sin ventas, empeños ni artículos asociados, cuando presiono "Eliminar" y<br>confirmo, entonces el cliente se elimina permanentemente (solo Admin).|
|---|---|
|**✓ **|Dado que el cliente tiene ventas, empeños o artículos asociados, entonces el sistema muestra "No se puede<br>eliminar: el cliente tiene operaciones o artículos registrados." y no procede.|
|**✓ **|Antes de eliminar permanentemente, se muestra un diálogo de confirmación con el nombre del cliente.|
|**✓ **|El Empleado puede cambiar el estado del cliente a "Eliminado" (soft-delete) y el cliente desaparece de su<br>vista.|


## **HU-3 — Módulo de Inventario (Artículos)**


Compra Venta © Abril 2026  |  Página 7


**Documento Técnico v4.0**

|HU-08 — Consultar inventario de artículos|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 1|3 pts|**Alta**|
|**Rol**|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|
|**Como...**|Quiero consultar el inventario de artículos con su estado, precio y stock|Quiero consultar el inventario de artículos con su estado, precio y stock|Quiero consultar el inventario de artículos con su estado, precio y stock|Quiero consultar el inventario de artículos con su estado, precio y stock|
|**Para...**|conocer qué artículos están disponibles antes de registrar una venta o empeño|conocer qué artículos están disponibles antes de registrar una venta o empeño|conocer qué artículos están disponibles antes de registrar una venta o empeño|conocer qué artículos están disponibles antes de registrar una venta o empeño|



**Reglas de Negocio**

   - El estado se deriva automáticamente: amount > 0 → Disponible; amount = 0 → Sin stock. No existe
columna "sold".


**Criterios de Aceptación**

|✓|El listado muestra: nombre del artículo, categoría, descripción, precio, cantidad y estado derivado<br>(Disponible / Sin stock).|
|---|---|
|**✓ **|El campo "estado" se calcula en consulta: amount > 0 → Disponible; amount = 0 → Sin stock.|
|**✓ **|El buscador filtra por nombre de artículo con coincidencia parcial (mínimo 2 caracteres, índice GIN).|
|**✓ **|El listado se carga en hilo secundario con indicador de carga visible.|


|HU-09 — Registrar nuevo artículo en inventario|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 1|5 pts|**Alta**|
|**Rol**|Administrador o Empleado|Administrador o Empleado|Administrador o Empleado|Administrador o Empleado|
|**Como...**|Quiero registrar un nuevo artículo en el inventario asociado a un cliente<br>propietario|Quiero registrar un nuevo artículo en el inventario asociado a un cliente<br>propietario|Quiero registrar un nuevo artículo en el inventario asociado a un cliente<br>propietario|Quiero registrar un nuevo artículo en el inventario asociado a un cliente<br>propietario|
|**Para...**|tener trazabilidad de quién trajo el artículo al negocio y gestionar su disposición<br>posterior|tener trazabilidad de quién trajo el artículo al negocio y gestionar su disposición<br>posterior|tener trazabilidad de quién trajo el artículo al negocio y gestionar su disposición<br>posterior|tener trazabilidad de quién trajo el artículo al negocio y gestionar su disposición<br>posterior|



**Reglas de Negocio**

   - La categoría es obligatoria y viene de un ENUM: Electronica, Joyeria, Ropa, Muebles, Otro.

   - El campo "sold" no existe en el modelo de datos (RF-03.3).

   - El precio solo puede ser modificado posteriormente por el Admin o por el Empleado con autorización
temporal.


**Criterios de Aceptación**

|✓|El formulario incluye: nombre (obligatorio), descripción, categoría (obligatoria), precio > 0, cantidad >= 0 y<br>cliente propietario (seleccionado de la lista).|
|---|---|
|**✓ **|Si el precio es 0 o negativo, el sistema muestra "El precio debe ser mayor a cero."|
|**✓ **|Si la cantidad es negativa, el sistema muestra "La cantidad no puede ser negativa."|
|**✓ **|Al guardar, el artículo aparece inmediatamente en el listado de inventario.|



Compra Venta © Abril 2026  |  Página 8


**Documento Técnico v4.0**


**Dependencias:** HU-05 (cliente debe existir)

|HU-10 — Editar artículo existente|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 2|3 pts|**Media**|
|**Rol**|Administrador (todos los campos) / Empleado (excepto precio sin autorización)|Administrador (todos los campos) / Empleado (excepto precio sin autorización)|Administrador (todos los campos) / Empleado (excepto precio sin autorización)|Administrador (todos los campos) / Empleado (excepto precio sin autorización)|
|**Como...**|Quiero editar el nombre, precio, categoría y descripción de un artículo existente|Quiero editar el nombre, precio, categoría y descripción de un artículo existente|Quiero editar el nombre, precio, categoría y descripción de un artículo existente|Quiero editar el nombre, precio, categoría y descripción de un artículo existente|
|**Para...**|corregir errores de registro o actualizar el precio de venta según condiciones del<br>mercado|corregir errores de registro o actualizar el precio de venta según condiciones del<br>mercado|corregir errores de registro o actualizar el precio de venta según condiciones del<br>mercado|corregir errores de registro o actualizar el precio de venta según condiciones del<br>mercado|



**Reglas de Negocio**

   - Si el Empleado desea modificar el precio, aparece un modal de autorización donde el Admin ingresa sus
credenciales. La autorización expira al guardar o al cambiar de pantalla.

   - La operación de cambio de precio con autorización se registra en audit_log con el profile_id del Admin
autorizante.

   - La cantidad no se puede modificar mediante edición directa; solo cambia por operaciones de venta,
empeño o ajuste de stock.


**Criterios de Aceptación**

|✓|El formulario de edición precarga los valores actuales del artículo.|
|---|---|
|**✓ **|No se puede modificar el campo "cantidad" mediante edición directa.|
|**✓ **|Las mismas validaciones de precio y cantidad aplican al editar.|
|**✓ **|El Empleado necesita autorización temporal del Admin para cambiar el precio. Los demás campos los puede<br>modificar directamente.|
|**△ **|La autorización expira inmediatamente tras guardar el cambio o al navegar a otra pantalla.|



**Dependencias:** HU-09

|HU-11 — Aumentar stock de un artículo|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 2|2 pts|**Media**|
|**Rol**|Administrador o Empleado|Administrador o Empleado|Administrador o Empleado|Administrador o Empleado|
|**Como...**|Quiero aumentar el stock de un artículo cuando ingresan más unidades|Quiero aumentar el stock de un artículo cuando ingresan más unidades|Quiero aumentar el stock de un artículo cuando ingresan más unidades|Quiero aumentar el stock de un artículo cuando ingresan más unidades|
|**Para...**|reflejar con precisión la disponibilidad real del inventario|reflejar con precisión la disponibilidad real del inventario|reflejar con precisión la disponibilidad real del inventario|reflejar con precisión la disponibilidad real del inventario|



**Reglas de Negocio**

   - La operación se registra en audit_log con valor anterior y nuevo.


**Criterios de Aceptación**


**✓** Dado que selecciono un artículo y presiono "Agregar stock", cuando ingreso una cantidad positiva y


Compra Venta © Abril 2026  |  Página 9


**Documento Técnico v4.0**

|Col1|confirmo, entonces el stock se incrementa en ese valor.|
|---|---|
|**✓ **|Solo se aceptan valores enteros positivos (> 0). Si se ingresa 0 o negativo, se muestra error.|
|**✓ **|La operación se registra en audit_log con el valor anterior y nuevo.|



**Dependencias:** HU-09

## **HU-4 — Módulo de Empeños (Pawns)**







|HU-12 — Registrar nuevo empeño|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 1|13 pts|**Alta**|
|**Rol**|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|
|**Como...**|Quiero registrar un nuevo empeño especificando artículo, cliente, cantidad,<br>precio, peso (si es joyería), número de cuotas, y fechas de empeño y<br>devolución|Quiero registrar un nuevo empeño especificando artículo, cliente, cantidad,<br>precio, peso (si es joyería), número de cuotas, y fechas de empeño y<br>devolución|Quiero registrar un nuevo empeño especificando artículo, cliente, cantidad,<br>precio, peso (si es joyería), número de cuotas, y fechas de empeño y<br>devolución|Quiero registrar un nuevo empeño especificando artículo, cliente, cantidad,<br>precio, peso (si es joyería), número de cuotas, y fechas de empeño y<br>devolución|
|**Para...**|documentar el acuerdo comercial de empeño con todos los datos necesarios<br>para su seguimiento y cobro|documentar el acuerdo comercial de empeño con todos los datos necesarios<br>para su seguimiento y cobro|documentar el acuerdo comercial de empeño con todos los datos necesarios<br>para su seguimiento y cobro|documentar el acuerdo comercial de empeño con todos los datos necesarios<br>para su seguimiento y cobro|


**Reglas de Negocio**

   - El peso en gramos (weight_grams) es obligatorio cuando la categoría del artículo es Joyería (validado en
UI y por trigger en BD).

   - La reducción de stock es atómica (transacción JDBC). Si el stock es insuficiente, el empeño no se
registra.

   - Estados del empeño: ACTIVO (vigente), VENCIDO (return_date < hoy sin pago), FINALIZADO (todas las
cuotas pagadas), PERDIDO (>4 cuotas consecutivas sin pagar), RETIRADO (cliente retiró antes de
vencer), VENDIDO (artículo vendido por vencimiento).

   - Si el cliente acumula más de 4 cuotas consecutivas sin pagar, el estado cambia automáticamente a
PERDIDO y el artículo puede ser vendido sin posibilidad de reclamo del cliente.


**Criterios de Aceptación**







|✓|El formulario incluye: artículo (selector de inventario disponible), cliente (selector), cantidad, precio del<br>empeño > 0, peso en gramos (obligatorio si Joyería), número de cuotas >= 1, fecha de empeño (default hoy)<br>y fecha de devolución.|
|---|---|
|**✓ **|La fecha de devolución debe ser estrictamente posterior a la fecha de empeño; de lo contrario se muestra un<br>error.|
|**✓ **|Al registrar, el sistema reduce automáticamente el stock del artículo en la cantidad indicada (operación<br>atómica).|
|**✓ **|Si el stock del artículo es insuficiente, el sistema muestra "Stock insuficiente para el artículo seleccionado." y<br>no registra el empeño.|
|**✓ **|El empeño registrado aparece inmediatamente en el listado de empeños activos.|
|**✓ **|El precio del empeño debe ser mayor a cero.|


**Dependencias:** HU-05, HU-09


Compra Venta © Abril 2026  |  Página 10


**Documento Técnico v4.0**

|HU-13 — Visualizar listado de empeños por estado|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 1|5 pts|**Alta**|
|**Rol**|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|
|**Como...**|Quiero visualizar el listado de empeños por estado y filtrarlos según mi rol|Quiero visualizar el listado de empeños por estado y filtrarlos según mi rol|Quiero visualizar el listado de empeños por estado y filtrarlos según mi rol|Quiero visualizar el listado de empeños por estado y filtrarlos según mi rol|
|**Para...**|hacer seguimiento de los empeños pendientes de devolución y actuar ante los<br>vencidos|hacer seguimiento de los empeños pendientes de devolución y actuar ante los<br>vencidos|hacer seguimiento de los empeños pendientes de devolución y actuar ante los<br>vencidos|hacer seguimiento de los empeños pendientes de devolución y actuar ante los<br>vencidos|



**Reglas de Negocio**

   - El Administrador ve todos los empeños. El Empleado ve los propios en escritura; puede consultar datos
básicos de empeños de compañeros (solo lectura).

   - RLS en Supabase como primera línea; capa de servicio como segunda.


**Criterios de Aceptación**

|✓|El Administrador ve todos los empeños con filtros: Activo, Vencido, Finalizado, Perdido, Retirado, Vendido,<br>Todos.|
|---|---|
|**✓ **|El Empleado ve sus empeños propios y puede consultar datos básicos (nombre del artículo, cliente, cuotas<br>pendientes, ID) de empeños de compañeros.|
|**✓ **|Cada fila muestra: artículo, cliente, empleado (solo Admin), fechas, precio y estado.|
|**✓ **|El listado muestra el valor total de empeños activos al pie del panel.|
|**✓ **|Si hay empeños vencidos, el indicador se resalta en color de alerta (rojo/naranja).|



**Dependencias:** HU-12

|HU-14 — Marcar empeño como devuelto|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 2|3 pts|**Alta**|
|**Rol**|Empleado (propios) o Administrador (todos)|Empleado (propios) o Administrador (todos)|Empleado (propios) o Administrador (todos)|Empleado (propios) o Administrador (todos)|
|**Como...**|Quiero marcar un empeño como devuelto cuando el cliente recupera su artículo|Quiero marcar un empeño como devuelto cuando el cliente recupera su artículo|Quiero marcar un empeño como devuelto cuando el cliente recupera su artículo|Quiero marcar un empeño como devuelto cuando el cliente recupera su artículo|
|**Para...**|cerrar el ciclo del empeño y reflejar correctamente el estado en el sistema|cerrar el ciclo del empeño y reflejar correctamente el estado en el sistema|cerrar el ciclo del empeño y reflejar correctamente el estado en el sistema|cerrar el ciclo del empeño y reflejar correctamente el estado en el sistema|



**Criterios de Aceptación**

|✓|Dado que selecciono un empeño activo propio y presiono "Marcar devuelto", cuando confirmo, entonces el<br>estado cambia a RETIRADO y el empeño desaparece del listado de activos.|
|---|---|
|**✓ **|No se puede marcar como devuelto un empeño ya finalizado, retirado, perdido o vendido (botón<br>deshabilitado).|
|**✓ **|La operación actualiza updated_at y se registra en audit_log.|



**Dependencias:** HU-12


Compra Venta © Abril 2026  |  Página 11


**Documento Técnico v4.0**

|HU-15 — Expiración automática y manual de<br>empeños|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 2|5 pts|**Alta**|
|**Rol**|Administrador (manual) / Sistema (automático al iniciar)|Administrador (manual) / Sistema (automático al iniciar)|Administrador (manual) / Sistema (automático al iniciar)|Administrador (manual) / Sistema (automático al iniciar)|
|**Como...**|Quiero marcar manualmente un empeño como expirado y que el sistema<br>procese automáticamente todos los vencidos al iniciarse|Quiero marcar manualmente un empeño como expirado y que el sistema<br>procese automáticamente todos los vencidos al iniciarse|Quiero marcar manualmente un empeño como expirado y que el sistema<br>procese automáticamente todos los vencidos al iniciarse|Quiero marcar manualmente un empeño como expirado y que el sistema<br>procese automáticamente todos los vencidos al iniciarse|
|**Para...**|gestionar los empeños que superaron su fecha límite sin devolución y mantener<br>el estado del sistema coherente|gestionar los empeños que superaron su fecha límite sin devolución y mantener<br>el estado del sistema coherente|gestionar los empeños que superaron su fecha límite sin devolución y mantener<br>el estado del sistema coherente|gestionar los empeños que superaron su fecha límite sin devolución y mantener<br>el estado del sistema coherente|



**Reglas de Negocio**

   - Solo el Admin puede marcar expirado manualmente. El proceso automático corre en cualquier instancia
al iniciar la app.

   - Al expirar un empeño, el stock del artículo NO se modifica (RF-04.7). El artículo queda en inventario para
nueva disposición.


**Criterios de Aceptación**

|✓|Al iniciar la aplicación, el sistema ejecuta fn_expire_overdue_pawns() que marca como Vencido todos los<br>empeños Activos con return_date < hoy.|
|---|---|
|**✓ **|La función es idempotente: si ya está vencido, no genera error ni duplicados.|
|**✓ **|El Admin puede marcar manualmente un empeño como Vencido desde la interfaz.|
|**✓ **|El proceso automático se ejecuta en hilo secundario (SwingWorker) y no bloquea el EDT de Swing.|
|**✓ **|Al expirar un empeño, el stock del artículo NO se modifica.|
|**✓ **|El número de empeños procesados se registra en audit_log.|


|HU-16 — Registrar pago de cuota de empeño|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 2|5 pts|**Alta**|
|**Rol**|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|
|**Como...**|Quiero registrar el pago de una cuota de un empeño activo|Quiero registrar el pago de una cuota de un empeño activo|Quiero registrar el pago de una cuota de un empeño activo|Quiero registrar el pago de una cuota de un empeño activo|
|**Para...**|mantener el control de pagos del cliente y que el sistema cambie el estado<br>automáticamente al completar todas las cuotas|mantener el control de pagos del cliente y que el sistema cambie el estado<br>automáticamente al completar todas las cuotas|mantener el control de pagos del cliente y que el sistema cambie el estado<br>automáticamente al completar todas las cuotas|mantener el control de pagos del cliente y que el sistema cambie el estado<br>automáticamente al completar todas las cuotas|



**Reglas de Negocio**

   - Cada pago se registra en la tabla pawn_payments y el trigger actualiza installments_paid en pawns.

   - Cuando installments_paid = installment_count, el estado cambia automáticamente a FINALIZADO.

   - Un pago reinicia el contador installments_missed a 0.


**Criterios de Aceptación**


Compra Venta © Abril 2026  |  Página 12


**Documento Técnico v4.0**

|✓|Dado que selecciono un empeño Activo o Vencido, cuando registro un pago con monto > 0, entonces se<br>crea un registro en pawn_payments y installments_paid se incrementa en 1.|
|---|---|
|**✓ **|Cuando installments_paid llega a installment_count, el estado del empeño cambia automáticamente a<br>FINALIZADO.|
|**✓ **|Un pago registrado reinicia el contador de cuotas perdidas (installments_missed = 0).|
|**✓ **|La operación se registra en audit_log.|



**Dependencias:** HU-12

|HU-17 — Registrar cuota impagada|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 2|3 pts|**Alta**|
|**Rol**|Administrador|Administrador|Administrador|Administrador|
|**Como...**|Quiero registrar que un cliente no pagó una cuota en la fecha acordada|Quiero registrar que un cliente no pagó una cuota en la fecha acordada|Quiero registrar que un cliente no pagó una cuota en la fecha acordada|Quiero registrar que un cliente no pagó una cuota en la fecha acordada|
|**Para...**|que el sistema incremente el contador de faltas y cambie automáticamente el<br>estado a PERDIDO si supera el límite|que el sistema incremente el contador de faltas y cambie automáticamente el<br>estado a PERDIDO si supera el límite|que el sistema incremente el contador de faltas y cambie automáticamente el<br>estado a PERDIDO si supera el límite|que el sistema incremente el contador de faltas y cambie automáticamente el<br>estado a PERDIDO si supera el límite|



**Reglas de Negocio**

   - Solo el Admin puede registrar cuotas impagadas manualmente.

   - Cuando installments_missed > 4, el estado cambia automáticamente a PERDIDO y el artículo queda
disponible para venta.


**Criterios de Aceptación**

|✓|Dado que selecciono un empeño Activo o Vencido y presiono "Registrar cuota impagada", cuando confirmo,<br>entonces installments_missed se incrementa en 1.|
|---|---|
|**✓ **|Cuando installments_missed > 4, el estado cambia automáticamente a PERDIDO.|
|**✓ **|El artículo de un empeño PERDIDO queda disponible para nueva venta (el stock no fue modificado al<br>expirar).|
|**✓ **|La operación se registra en audit_log.|



**Dependencias:** HU-12

|HU-18 — Eliminar empeño|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 3|2 pts|**Baja**|
|**Rol**|Administrador|Administrador|Administrador|Administrador|
|**Como...**|Quiero eliminar un empeño del sistema|Quiero eliminar un empeño del sistema|Quiero eliminar un empeño del sistema|Quiero eliminar un empeño del sistema|
|**Para...**|corregir registros erróneos que no deberían existir|corregir registros erróneos que no deberían existir|corregir registros erróneos que no deberían existir|corregir registros erróneos que no deberían existir|



**Criterios de Aceptación**


Compra Venta © Abril 2026  |  Página 13


**Documento Técnico v4.0**

|✓|Solo el Administrador puede eliminar empeños.|
|---|---|
|**✓ **|Antes de eliminar, se muestra un diálogo de confirmación con detalles del empeño.|
|**✓ **|La eliminación se registra en audit_log con los datos anteriores (old_data en JSON).|


## **HU-5 — Módulo de Ventas**

|HU-19 — Registrar venta atómica|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 2|13 pts|**Alta**|
|**Rol**|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|
|**Como...**|Quiero registrar una venta asociando un cliente, uno o más artículos con sus<br>cantidades y precios|Quiero registrar una venta asociando un cliente, uno o más artículos con sus<br>cantidades y precios|Quiero registrar una venta asociando un cliente, uno o más artículos con sus<br>cantidades y precios|Quiero registrar una venta asociando un cliente, uno o más artículos con sus<br>cantidades y precios|
|**Para...**|documentar formalmente cada transacción de venta y actualizar el inventario de<br>forma automática y atómica|documentar formalmente cada transacción de venta y actualizar el inventario de<br>forma automática y atómica|documentar formalmente cada transacción de venta y actualizar el inventario de<br>forma automática y atómica|documentar formalmente cada transacción de venta y actualizar el inventario de<br>forma automática y atómica|



**Reglas de Negocio**

   - Atomicidad garantizada mediante stored procedure register_sale() con transacción PostgreSQL y lock
pesimista FOR UPDATE.

   - Si cualquier artículo tiene stock insuficiente, toda la venta hace rollback y se informa qué artículo falló.


**Criterios de Aceptación**

|✓|La venta incluye: cliente (seleccionado), fecha (default ahora) y al menos un artículo con cantidad > 0 y<br>precio unitario > 0.|
|---|---|
|**✓ **|No se permite el mismo article_id dos veces en la misma venta (UNIQUE constraint en sales_details).|
|**✓ **|Si el stock de algún artículo es insuficiente, la venta completa se cancela (rollback) y se muestra qué artículo<br>carece de stock.|
|**✓ **|La operación es atómica: se usa la stored procedure register_sale() con rollback explícito.|
|**✓ **|Al completar la venta, el stock de cada artículo vendido se reduce automáticamente.|
|**✓ **|La venta registrada aparece inmediatamente en el historial de ventas.|
|**✓ **|El sistema ofrece generar factura PDF al completar la venta.|



**Dependencias:** HU-05, HU-09

|HU-20 — Consultar historial de ventas|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 3|5 pts|**Media**|
|**Rol**|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|Empleado o Administrador|
|**Como...**|Quiero consultar el historial de ventas filtrado por fecha, cliente o empleado|Quiero consultar el historial de ventas filtrado por fecha, cliente o empleado|Quiero consultar el historial de ventas filtrado por fecha, cliente o empleado|Quiero consultar el historial de ventas filtrado por fecha, cliente o empleado|



Compra Venta © Abril 2026  |  Página 14


**Documento Técnico v4.0**


**Reglas de Negocio**

   - El empleado solo ve sus propias ventas. El Admin ve todas y puede filtrar por empleado.


**Criterios de Aceptación**

|✓|Los filtros disponibles son: rango de fechas, cliente y empleado (este último solo visible para Admin).|
|---|---|
|**✓ **|Cada venta en el listado muestra: fecha, cliente, empleado, número de artículos y total calculado.|
|**✓ **|El total de la venta se calcula como SUM(amount * unit_price) por venta.|
|**✓ **|El listado carga en hilo secundario con indicador de progreso.|



**Dependencias:** HU-19

|HU-21 — Eliminar venta registrada erróneamente|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 3|3 pts|**Baja**|
|**Rol**|Administrador|Administrador|Administrador|Administrador|
|**Como...**|Quiero eliminar una venta registrada erróneamente|Quiero eliminar una venta registrada erróneamente|Quiero eliminar una venta registrada erróneamente|Quiero eliminar una venta registrada erróneamente|
|**Para...**|corregir transacciones que se registraron por error, manteniendo la integridad<br>del historial|corregir transacciones que se registraron por error, manteniendo la integridad<br>del historial|corregir transacciones que se registraron por error, manteniendo la integridad<br>del historial|corregir transacciones que se registraron por error, manteniendo la integridad<br>del historial|



**Reglas de Negocio**

   - El stock NO se restaura automáticamente al eliminar una venta; esto debe coordinarse manualmente.


**Criterios de Aceptación**

|✓|Solo el Administrador puede eliminar ventas.|
|---|---|
|**✓ **|Antes de eliminar, se muestra diálogo de confirmación con el total y la fecha de la venta.|
|**✓ **|La eliminación en cascada borra automáticamente los sales_details asociados.|
|**✓ **|La operación se registra en audit_log.|


## **HU-6 — Gestión de Empleados**

|HU-22 — Gestionar estado de empleados|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 2|2 pts|**Media**|
|**Rol**|Administrador|Administrador|Administrador|Administrador|
|**Como...**|Quiero ver el listado de todos los empleados registrados y activar o desactivar|Quiero ver el listado de todos los empleados registrados y activar o desactivar|Quiero ver el listado de todos los empleados registrados y activar o desactivar|Quiero ver el listado de todos los empleados registrados y activar o desactivar|



Compra Venta © Abril 2026  |  Página 15


**Documento Técnico v4.0**

|Col1|su acceso al sistema|
|---|---|
|**Para...**|tener visibilidad del equipo y gestionar el acceso al sistema de cada miembro|



**Criterios de Aceptación**

|✓|El listado muestra: nombre completo, correo, rol y estado (Activo / Inactivo), ordenado por nombre completo.|
|---|---|
|**✓ **|El Administrador puede activar o desactivar cualquier empleado con un solo clic.|
|**✓ **|Un empleado desactivado (active = false) no puede iniciar sesión.|
|**✓ **|El listado se carga en hilo secundario.|



**Dependencias:** HU-02

## **HU-7 — Dashboard Ejecutivo**

|HU-23 — Ver dashboard con métricas clave|Col2|Sprint|Puntos|Prioridad|
|---|---|---|---|---|
|||Sprint 1|5 pts|**Alta**|
|**Rol**|Administrador o Empleado|Administrador o Empleado|Administrador o Empleado|Administrador o Empleado|
|**Como...**|Quiero ver un dashboard con métricas clave al ingresar al sistema|Quiero ver un dashboard con métricas clave al ingresar al sistema|Quiero ver un dashboard con métricas clave al ingresar al sistema|Quiero ver un dashboard con métricas clave al ingresar al sistema|
|**Para...**|tener una visión rápida del estado del negocio sin navegar por múltiples<br>módulos|tener una visión rápida del estado del negocio sin navegar por múltiples<br>módulos|tener una visión rápida del estado del negocio sin navegar por múltiples<br>módulos|tener una visión rápida del estado del negocio sin navegar por múltiples<br>módulos|



**Reglas de Negocio**

   - Los datos se cargan desde la vista v_dashboard en una sola query SQL optimizada.


**Criterios de Aceptación**

|✓|El dashboard muestra: total de artículos en stock, empeños activos (count), valor total de empeños activos<br>(suma), empeños vencidos (count) y total de clientes registrados.|
|---|---|
|**✓ **|Los datos se cargan desde la vista v_dashboard en una sola consulta SQL.|
|**✓ **|Las métricas se actualizan al navegar de regreso al dashboard.|
|**✓ **|Si hay empeños vencidos (count > 0), ese indicador se resalta en color de alerta (rojo/naranja).|
|**✓ **|La carga del dashboard ocurre en un SwingWorker; se muestra spinner mientras carga.|


## **HU-8 — Facturación PDF (NUEVO)**

|HU-24 — Generar factura PDF de venta o empeño|Sprint|Puntos|Prioridad|
|---|---|---|---|
||Sprint 2|5 pts|**Media**|



Compra Venta © Abril 2026  |  Página 16


**Documento Técnico v4.0**

|Rol|Empleado o Administrador|
|---|---|
|**Como...**|Quiero generar una factura PDF para una venta completada o un empeño<br>registrado|
|**Para...**|entregar al cliente un comprobante formal de la operación con todos los datos<br>del acuerdo|



**Reglas de Negocio**

   - La numeración es única y secuencial por día: FACT-YYYYMMDD-NNNN. Thread-safe mediante
next_invoice_number().

   - El PDF se genera en memoria (bytes) y se ofrece al usuario para guardar o imprimir. No se persiste
automáticamente en disco.

   - Implementación con iText 7 (AGPL) o Apache PDFBox (Apache 2.0) según licencia disponible.


**Criterios de Aceptación**

|✓|Dado que completo una venta o empeño, cuando presiono "Generar factura", entonces el sistema produce<br>un PDF con número único FACT-YYYYMMDD-NNNN.|
|---|---|
|**✓ **|La factura incluye: datos del negocio, cliente, empleado, artículos con precios, total y (para empeños)<br>número de cuotas y fecha de vencimiento.|
|**✓ **|El PDF se genera en menos de 3 segundos para ventas con hasta 20 artículos.|
|**✓ **|El usuario puede guardar o imprimir el PDF desde un diálogo de sistema de archivos.|



**Dependencias:** HU-19 o HU-12


Compra Venta © Abril 2026  |  Página 17


**Documento Técnico v4.0**

# **3. Requisitos Funcionales Consolidados**

## **3.1 Módulo de Autenticación — RF-01**




















|ID|Descripción|Prior.|HU|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**RF-0**<br>**1.1**|El sistema debe autenticar al<br>usuario mediante correo y<br>contraseña usando el<br>endpoint /auth/v1/token de<br>Supabase Auth.|**Alta**|HU-0<br>1|email,<br>password|POST a Supabase<br>Auth, recuperar JWT|Token JWT,<br>perfil cargado|
|**RF-0**<br>**1.2**|Tras autenticación exitosa, el<br>sistema debe recuperar el<br>perfil (nombre, rol, active)<br>desde la tabla profile<br>mediante el UUID del usuario.|**Alta**|HU-0<br>1|UUID del<br>usuario<br>autenticado|SELECT en profile por<br>UUID|Objeto Profile en<br>SessionManager|
|**RF-0**<br>**1.3**|Si profile.active = false, el<br>sistema debe denegar el<br>acceso y mostrar el mensaje<br>"Tu cuenta está desactivada."<br>sin cargar ninguna pantalla<br>protegida.|**Alta**|HU-0<br>1|Valor<br>profile.active|Verificar active antes<br>de navegar|Mensaje de<br>error, pantalla<br>de login|
|**RF-0**<br>**1.4**|El Administrador debe poder<br>registrar un nuevo usuario<br>enviando {email, password,<br>full_name, rol} en<br>raw_user_meta_data al<br>endpoint de Supabase Auth.|**Alta**|HU-0<br>2|Formulario<br>de registro|POST a<br>/auth/v1/admin/users,<br>trigger<br>on_auth_user_created|Perfil creado con<br>rol correcto|
|**RF-0**<br>**1.5**|El sistema debe permitir<br>cerrar sesión, invalidando el<br>token en Supabase y<br>limpiando el SessionManager<br>en memoria.|**Alta**|HU-0<br>3|Acción de<br>logout|POST a<br>/auth/v1/logout,<br>SessionManager.clear(<br>)|Pantalla de<br>login, sin datos<br>de sesión|
|**RF-0**<br>**1.6**|SessionManager debe<br>implementarse como<br>Singleton thread-safe (volatile<br>+ double-checked locking o<br>eager initialization).|**Alta**|HU-0<br>1,<br>HU-0<br>3|N/A (diseño)|Patrón Singleton con<br>DCL|Instancia única<br>thread-safe|


## **3.2 Módulo de Clientes — RF-02**















|ID|Descripción|Prior<br>.|HU|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**RF-0**<br>**2.1**|El sistema debe listar todos los<br>clientes ordenados por last_name<br>ASC, first_name ASC. Los<br>empleados ven solo status = Activo;<br>el Admin puede filtrar por status.|**Alta**|HU-04|Rol del<br>usuario<br>autenticado|SELECT con<br>ORDER BY y filtro<br>de status|Lista<br>paginada de<br>clientes|
|**RF-0**<br>**2.2**|El sistema debe filtrar clientes por<br>nombre, apellido o correo con<br>búsqueda parcial case-insensitive<br>usando índices GIN pg_trgm<br>(debounce 300ms en UI).|**Alta**|HU-04|Cadena de<br>búsqueda<br>(mín. 2 chars)|ILIKE o operador<br>% de pg_trgm|Resultados<br>filtrados|
|**RF-0**|El sistema debe permitir registrar un|**Alta**|HU-05|Formulario de|INSERT con|Cliente|


Compra Venta © Abril 2026  |  Página 18


**Documento Técnico v4.0**






















|ID|Descripción|Prior<br>.|HU|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**2.3**|cliente con nombre, apellido, correo<br>único y teléfono con formato válido.<br>Tanto Admin como Empleado<br>pueden crear clientes.|||cliente|validaciones,<br>status = Activo|persistido|
|**RF-0**<br>**2.4**|El correo debe validarse con<br>expresión regular RFC-5321 antes<br>de enviarse al servicio.|**Alta**|HU-05|String de<br>correo|Regex en<br>ClienteValidator|Error o paso<br>a servicio|
|**RF-0**<br>**2.5**|El teléfono solo acepta dígitos, "+", y<br>entre 7-15 caracteres (CHECK en<br>BD + validación en UI).|**Alta**|HU-05|String de<br>teléfono|Regex en UI +<br>CHECK en BD|Error o valor<br>válido|
|**RF-0**<br>**2.6**|Solo el Administrador puede editar o<br>eliminar clientes permanentemente.<br>El Empleado puede realizar soft-<br>delete (status = Eliminado).|**Alta**|HU-06<br>, <br>HU-07|Rol del<br>usuario|Verificar rol en<br>Service antes de<br>UPDATE/DELETE|Acción<br>ejecutada o<br>error de<br>permisos|
|**RF-0**<br>**2.7**|El sistema debe impedir eliminar<br>físicamente un cliente que tenga<br>ventas, empeños o artículos<br>asociados, mostrando un mensaje<br>descriptivo.|**Alta**|HU-07|ID del cliente|Verificar FK antes<br>de DELETE|"No se<br>puede<br>eliminar: el<br>cliente tiene<br>operaciones<br>registradas."|


## **3.3 Módulo de Artículos — RF-03**












|ID|Descripción|Prior.|HU|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**RF-0**<br>**3.1**|El sistema debe listar artículos<br>ordenados por name_article ASC. El<br>estado se deriva: amount > 0 →<br>Disponible; amount = 0 → Sin stock.|**Alta**|HU-08|N/A|SELECT con<br>ORDER BY,<br>estado<br>calculado|Lista de<br>artículos con<br>estado|
|**RF-0**<br>**3.2**|El buscador de artículos debe<br>soportar coincidencia parcial case-<br>insensitive sobre name_article<br>(mínimo 2 caracteres, índice GIN).|**Alta**|HU-08|String de<br>búsqueda|ILIKE con<br>índice GIN<br>gin_trgm_ops|Artículos<br>filtrados|
|**RF-0**<br>**3.3**|El campo "sold" no debe existir en el<br>modelo de datos. El estado se deriva<br>exclusivamente de amount.|**Alta**|HU-09|N/A<br>(arquitectural)|Verificar que<br>columna sold<br>no existe en<br>articles|Modelo sin<br>campo sold|
|**RF-0**<br>**3.4**|Tanto el Administrador como el<br>Empleado pueden crear artículos.<br>Campos obligatorios: nombre,<br>categoría, precio (> 0), cantidad (>=<br>0) y cliente_id.|**Alta**|HU-09|Formulario de<br>artículo|INSERT con<br>validaciones|Artículo<br>creado en<br>inventario|
|**RF-0**<br>**3.5**|Admin puede editar cualquier campo.<br>Empleado puede editar nombre,<br>descripción y categoría. Para editar<br>precio, el Empleado requiere<br>autorización temporal del Admin.|**Medi**<br>**a**|HU-10|Formulario de<br>edición + rol|UPDATE con<br>verificación de<br>permisos por<br>campo|Artículo<br>actualizado|
|**RF-0**<br>**3.6**|El sistema debe permitir incrementar<br>el stock de un artículo (addStock)<br>solo con valores enteros positivos (><br>0).|**Medi**<br>**a**|HU-11|ID artículo +<br>cantidad|UPDATE<br>amount +=<br>cantidad,<br>INSERT|Stock<br>actualizado,<br>audit<br>registrado|



Compra Venta © Abril 2026  |  Página 19


**Documento Técnico v4.0**
















|ID|Descripción|Prior.|HU|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
||||||audit_log||
|**RF-0**<br>**3.7**|El stock se reduce automáticamente<br>al registrar una venta o empeño,<br>dentro de la misma transacción<br>(stored procedure o JDBC con<br>setAutoCommit(false)).|**Alta**|HU-12,<br>HU-19|ID artículo +<br>cantidad a<br>reducir|UPDATE<br>amount -=<br>cantidad con<br>lock FOR<br>UPDATE|Stock<br>reducido<br>atómicamente|
|**RF-0**<br>**3.8**|El sistema debe impedir eliminar un<br>artículo que tenga empeños o<br>detalles de venta asociados (ON<br>DELETE RESTRICT en FK).|**Alta**|HU-09|ID artículo|Verificar FK<br>antes de<br>DELETE|Error<br>descriptivo si<br>hay<br>dependencias|
|**RF-0**<br>**3.9**|La autorización temporal de Admin<br>para cambio de precio por Empleado<br>se implementa como modal de<br>credenciales que valida contra<br>Supabase Auth. Expira al guardar o<br>cambiar de pantalla.|**Medi**<br>**a**|HU-10|Credenciales<br>del Admin|POST a<br>Supabase<br>Auth, token<br>temporal,<br>INSERT<br>audit_log con<br>profile_id del<br>Admin|Precio<br>actualizado,<br>sesión de<br>autorización<br>cerrada|


## **3.4 Módulo de Empeños — RF-04**





























|ID|Descripción|Prio<br>r.|H<br>U|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**RF-**<br>**04.1**|El Admin ve todos los<br>empeños. El Empleado ve<br>los propios y puede<br>consultar datos básicos de<br>los ajenos (profile_id =<br>auth.uid() en escritura). RLS<br>como primera línea.|**Alta**|H<br>U-<br>13|Rol del usuario<br>autenticado|RLS en BD + filtro en capa<br>servicio|Empeños<br>según rol|
|**RF-**<br>**04.2**|Los empeños se listan por<br>estado usando ENUM<br>pawn_status. Los filtros<br>disponibles son: Activo,<br>Vencido, Finalizado,<br>Perdido, Retirado, Vendido,<br>Todos.|**Alta**|H<br>U-<br>13|Filtro de estado|SELECT con WHERE<br>status = ?|Lista filtrada|
|**RF-**<br>**04.3**|Al registrar un empeño,<br>return_date debe ser<br>estrictamente posterior a<br>pawn_date (validación en UI<br>y CHECK en BD).|**Alta**|H<br>U-<br>12|Fechas de<br>empeño y<br>devolución|CHECK constraint +<br>validación en UI|Error o registro<br>exitoso|
|**RF-**<br>**04.4**|El registro de un empeño<br>reduce el stock del artículo<br>en la cantidad indicada de<br>forma atómica (JDBC<br>transaction).|**Alta**|H<br>U-<br>12|ID artículo +<br>cantidad|UPDATE articles dentro<br>de misma transacción<br>JDBC|Stock reducido<br>o rollback con<br>mensaje|
|**RF-**<br>**04.5**|El sistema debe marcar un<br>empeño como Retirado<br>(devuelto) actualizando<br>updated_at y registrando en<br>audit_log.|**Alta**|H<br>U-<br>14|ID empeño,<br>empleado<br>autenticado|UPDATE status =<br>Retirado, INSERT<br>audit_log|Estado<br>actualizado,<br>audit registrado|
|**RF-**|Al iniciar la aplicación, el|**Alta**|H|Fecha actual del|SELECT empeños Activos|Empeños|


Compra Venta © Abril 2026  |  Página 20


**Documento Técnico v4.0**


























|ID|Descripción|Prio<br>r.|H<br>U|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**04.6**|sistema ejecuta<br>fn_expire_overdue_pawns()<br>de forma idempotente en<br>hilo secundario<br>(SwingWorker).||U-<br>15|sistema|con return_date < hoy,<br>UPDATE status = Vencido|vencidos<br>actualizados|
|**RF-**<br>**04.7**|Al expirar un empeño<br>(automático o manual), el<br>stock del artículo NO se<br>modifica.|**Alta**|H<br>U-<br>15|Empeños a<br>expirar|UPDATE status<br>únicamente, sin tocar<br>articles.amount|Stock intacto<br>tras expiración|
|**RF-**<br>**04.8**|Solo el Administrador puede<br>marcar empeños como<br>Vencidos manualmente o<br>eliminar empeños.|**Alta**|H<br>U-<br>15,<br>H<br>U-<br>18|Rol del usuario|Verificar rol en<br>PawnService|Acción<br>ejecutada o<br>error de<br>permisos|
|**RF-**<br>**04.1**<br>**0**|El sistema debe registrar<br>cada pago de cuota en<br>pawn_payments. El trigger<br>update_pawn_status_on_pa<br>yment incrementa<br>installments_paid y reinicia<br>installments_missed.|**Alta**|H<br>U-<br>16|pawn_id, monto,<br>fecha|INSERT pawn_payments<br>→ trigger actualiza pawns|Pago<br>registrado,<br>estado empeño<br>actualizado si<br>aplica|
|**RF-**<br>**04.1**<br>**1**|Cuando installments_paid =<br>installment_count, el estado<br>del empeño cambia<br>automáticamente a<br>FINALIZADO (trigger en<br>pawn_payments).|**Alta**|H<br>U-<br>16|installments_pai<br>d tras pago|Trigger compara paid con<br>count|status =<br>Finalizado|
|**RF-**<br>**04.1**<br>**2**|Cuando installments_missed<br>> 4 de forma consecutiva, el<br>sistema cambia el estado a<br>PERDIDO. El artículo queda<br>disponible para venta.|**Alta**|H<br>U-<br>17|installments_mis<br>sed|fn_mark_pawn_lost()<br>llamada desde servicio|status =<br>Perdido,<br>artículo en<br>inventario<br>disponible|
|**RF-**<br>**04.1**<br>**3**|El peso en gramos<br>(weight_grams) es<br>obligatorio cuando la<br>categoría del artículo es<br>Joyería. Validado en UI y<br>por trigger en BD.|**Alta**|H<br>U-<br>12|category del<br>artículo,<br>weight_grams|Trigger<br>validate_pawn_jewelry_we<br>ight()|Error<br>ERRCODE=CV<br>002 o registro<br>exitoso|


## **3.5 Módulo de Ventas — RF-05**
















|ID|Descripción|Prior.|HU|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**RF-0**<br>**5.1**|Una venta debe incluir al menos<br>un artículo. No se permite el<br>mismo article_id dos veces en la<br>misma venta (UNIQUE constraint<br>en sales_details).|**Alta**|HU-19|Lista de<br>artículos|Validación UI +<br>UNIQUE(sale_id,<br>article_id) en BD|Error o<br>venta<br>procesada|
|**RF-0**<br>**5.2**|El registro de una venta debe ser<br>atómico: si algún artículo no tiene<br>stock suficiente, la operación<br>completa hace rollback.|**Alta**|HU-19|Lista de<br>artículos con<br>cantidades|register_sale() con<br>lock FOR UPDATE y<br>RAISE EXCEPTION|Rollback<br>total o venta<br>completada|



Compra Venta © Abril 2026  |  Página 21


**Documento Técnico v4.0**






















|ID|Descripción|Prior.|HU|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**RF-0**<br>**5.3**|La atomicidad se implementa<br>mediante la stored procedure<br>register_sale() con transacción<br>PostgreSQL implícita.|**Alta**|HU-19|p_profile_id,<br>p_cliente_id,<br>p_items<br>(JSONB)|Función PL/pgSQL<br>con<br>BEGIN/EXCEPTION<br>implícito|sale_id o<br>excepción<br>CV001|
|**RF-0**<br>**5.4**|El historial de ventas debe ser<br>filtrable por rango de fechas y<br>cliente. Admin puede filtrar<br>también por empleado.|**Medi**<br>**a**|HU-20|Filtros de<br>consulta|SELECT con WHERE<br>dinámico, índice<br>compuesto<br>idx_sales_profile_date|Lista de<br>ventas<br>filtradas|
|**RF-0**<br>**5.5**|El total de cada venta se calcula<br>como SUM(sd.amount *<br>sd.unit_price) y se muestra en el<br>listado.|**Medi**<br>**a**|HU-20|sale_id|JOIN sales_details<br>con SUM|Total<br>calculado<br>por venta|
|**RF-0**<br>**5.6**|Solo el Administrador puede<br>eliminar ventas. La eliminación<br>borra en cascada los<br>sales_details (ON DELETE<br>CASCADE).|**Baja**|HU-21|sale_id, rol<br>Admin|DELETE sales +<br>cascade a<br>sales_details +<br>INSERT audit_log|Venta y<br>detalles<br>eliminados,<br>audit<br>registrado|


## **3.6 Módulo de Empleados — RF-06**
















|ID|Descripción|Prior.|HU|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**RF-0**<br>**6.1**|El Administrador puede listar todos los<br>perfiles ordenados por full_name ASC.|**Medi**<br>**a**|HU-22|N/A|SELECT<br>profile ORDER<br>BY full_name|Lista de<br>empleados|
|**RF-0**<br>**6.2**|El Administrador puede activar o<br>desactivar un perfil (active =<br>true/false). El cambio surte efecto en<br>el siguiente intento de login del<br>empleado.|**Medi**<br>**a**|HU-22|profile_id,<br>nuevo valor de<br>active|UPDATE<br>profile SET<br>active = ?|Estado<br>actualizado|


## **3.7 Dashboard — RF-07**




















|ID|Descripción|Prior.|HU|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**RF-0**<br>**7.1**|El dashboard se nutre de la<br>vista v_dashboard con una<br>sola query y muestra:<br>artículos en stock, empeños<br>activos, valor de empeños<br>activos, empeños vencidos<br>y total de clientes activos.|**Alta**|HU-2<br>3|N/A|SELECT * FROM<br>v_dashboard|Objeto<br>DashboardDTO|
|**RF-0**<br>**7.2**|Si el conteo de empeños<br>vencidos es > 0, ese campo<br>se muestra con estilo de<br>alerta (color rojo/naranja)<br>en la UI.|**Alta**|HU-2<br>3|empenos_vencido<br>s del DTO|Lógica de<br>presentación en<br>DashboardPresenter|Componente UI<br>con color de<br>alerta|
|**RF-0**<br>**7.3**|Los datos del dashboard se<br>recargan cada vez que el<br>usuario navega a la pantalla<br>principal (SwingWorker).|**Medi**<br>**a**|HU-2<br>3|Evento de<br>navegación|SwingWorker<br>ejecuta SELECT<br>v_dashboard|UI actualizada|


## **3.8 Facturación PDF — RF-08 (NUEVO)**

Compra Venta © Abril 2026  |  Página 22


**Documento Técnico v4.0**














|ID|Descripción|Prior.|HU|Entradas|Proceso|Salidas|
|---|---|---|---|---|---|---|
|**RF-**<br>**08.1**|El sistema debe generar<br>facturas PDF para ventas y<br>empeños usando iText 7 o<br>Apache PDFBox según<br>licencia disponible.|**Media**|HU-<br>24|InvoiceData<br>(sale o pawn<br>DTO)|InvoiceGenerator.generate(data)<br>→ byte[]|PDF en<br>memoria<br>como<br>byte[]|
|**RF-**<br>**08.2**|El número de factura es<br>único y secuencial por día<br>(FACT-YYYYMMDD-<br>NNNN), generado por<br>next_invoice_number()<br>thread-safe.|**Media**|HU-<br>24|Fecha de<br>emisión|INSERT ON CONFLICT DO<br>UPDATE en invoice_sequence|Número<br>único de<br>factura|
|**RF-**<br>**08.3**|Las facturas se generan en<br>memoria y se ofrecen al<br>usuario para<br>guardar/imprimir. No se<br>persisten automáticamente<br>en disco.|**Media**|HU-<br>24|byte[] del<br>PDF|JFileChooser para guardar o<br>PrintJob para imprimir|PDF<br>guardado<br>o impreso|



Compra Venta © Abril 2026  |  Página 23


**Documento Técnico v4.0**

# **4. Requisitos No Funcionales**

## **4.1 Seguridad — RNF-01**










|ID|Categoría|Descripción|Umbral|Verificación|
|---|---|---|---|---|
|RNF<br>-01.1|Autenticación|Las contraseñas son manejadas<br>exclusivamente por Supabase<br>Auth. El sistema nunca las<br>almacena, loguea ni transmite en<br>texto plano.|0 ocurrencias de<br>contraseña en logs<br>o código|Revisión de<br>código + análisis<br>estático<br>(SonarQube)|
|RNF<br>-01.2|Sesión|Los tokens JWT se almacenan<br>únicamente en memoria RAM<br>(SessionManager). No se<br>persisten en archivos, BD local ni<br>clipboard.|No existe ningún<br>archivo de sesión en<br>disco tras logout|Verificar<br>filesystem post-<br>logout + code<br>review|
|RNF<br>-01.3|Autorización|Cada operación sensible<br>(eliminar, editar precio, exportar)<br>verifica el rol del usuario en<br>SessionManager antes de<br>ejecutarse. RLS como primera<br>línea.|Empleado no puede<br>invocar operaciones<br>de Admin, aunque<br>manipule la UI|Pruebas de<br>penetración de<br>capa de servicio|
|RNF<br>-01.4|Configuración|Las cadenas de conexión, API<br>keys y Supabase URL se cargan<br>exclusivamente desde variables<br>de entorno o archivo .env nunca<br>incluido en el repositorio<br>(.gitignore).|0 credenciales<br>hardcodeadas en el<br>código fuente|git grep para<br>credenciales +<br>análisis estático|
|RNF<br>-01.5|RLS|Las políticas de Row Level<br>Security en Supabase<br>diferencian por rol para pawns,<br>profile y audit_log. El Empleado<br>no puede leer empeños ajenos<br>directamente desde Supabase.|Empleado no puede<br>leer empeños de<br>otros empleados<br>con query directa<br>usando su JWT|Test de query<br>directa con JWT<br>de Empleado|


## **4.2 Rendimiento — RNF-02**












|ID|Categoría|Descripción|Umbral|Verificación|
|---|---|---|---|---|
|RNF<br>-02.1|Concurrencia<br>UI|Toda operación de base de<br>datos se ejecuta fuera del EDT<br>de Swing usando SwingWorker.<br>0 excepciones de operaciones<br>BD en EDT.|0 operaciones BD en<br>el EDT|Profiling con Java<br>Mission Control|
|RNF<br>-02.2|Pool de<br>conexiones|HikariCP configurado con<br>maximumPoolSize <= 10,<br>connectionTimeout = 30s,<br>idleTimeout = 600s.|Ninguna operación<br>espera más de 30s<br>por conexión en<br>condiciones<br>normales|Test de carga con<br>conexiones<br>simultáneas|



Compra Venta © Abril 2026  |  Página 24


**Documento Técnico v4.0**














|ID|Categoría|Descripción|Umbral|Verificación|
|---|---|---|---|---|
|RNF<br>-02.3|Prepared<br>Statements|Todas las consultas a BD<br>deben usar PreparedStatement<br>con parametrización. Prohibida<br>la concatenación de valores de<br>usuario en SQL.|0 instancias de<br>concatenación de<br>strings en SQL|Análisis estático +<br>revisión de código|
|RNF<br>-02.4|Tiempo de<br>respuesta|Las operaciones de consulta<br>(listados) deben completar y<br>actualizar la UI en menos de 3<br>segundos bajo condiciones<br>normales de red.|p95 < 3s medido en<br>red local con<br>latencia < 50ms|Benchmarking<br>manual con<br>System.nanoTime()|
|RNF<br>-02.5|Dashboard|El dashboard carga desde<br>v_dashboard en una única<br>query en menos de 2 segundos.|p95 de carga del<br>dashboard < 2s|Medición con<br>System.nanoTime()<br>en SwingWorker|


## **4.3 Usabilidad — RNF-03**


















|ID|Categoría|Descripción|Umbral|Verificación|
|---|---|---|---|---|
|RNF-<br>03.1|Mensajes de<br>error|Los mensajes de error<br>mostrados al usuario deben<br>estar en español, ser<br>comprensibles para no técnicos<br>y sin stack traces ni códigos<br>SQL.|0 stack traces<br>visibles en<br>producción|Revisión manual<br>de todos los catch<br>blocks de la capa<br>UI|
|RNF-<br>03.2|Indicador de<br>carga|Toda operación que tarde más<br>de 500ms debe mostrar un<br>indicador de progreso (spinner o<br>barra) y deshabilitar controles<br>durante la carga.|100% de<br>operaciones<br>asíncronas con<br>indicador visible|Test de UX<br>manual + revisión<br>de SwingWorkers|
|RNF-<br>03.3|Validación UI|Los formularios validan la<br>entrada en la capa UI antes de<br>invocar el servicio. Los campos<br>inválidos se resaltan con<br>mensaje inline.|0 llamadas al<br>servicio con datos<br>inválidos|Test unitario en<br>cada clase<br>Validator|
|RNF-<br>03.4|Confirmación<br>destructiva|Toda operación de eliminación<br>muestra un diálogo de<br>confirmación con el nombre del<br>registro afectado antes de<br>proceder.|100% de acciones<br>de eliminar tienen<br>diálogo de<br>confirmación|Revisión manual<br>de cada flujo de<br>eliminación|
|RNF-<br>03.5|Accesibilidad<br>básica|Los campos de formulario deben<br>tener JLabel asociados y el<br>orden de tabulación debe ser<br>lógico (arriba-abajo, izquierda-<br>derecha).|Navegación<br>completa del<br>formulario sin ratón<br>posible|Test de<br>navegación por<br>teclado|


## **4.4 Mantenibilidad — RNF-04**

Compra Venta © Abril 2026  |  Página 25


**Documento Técnico v4.0**


















|ID|Categoría|Descripción|Umbral|Verificación|
|---|---|---|---|---|
|RNF<br>-04.1|Arquitectura<br>en capas|Separación estricta: UI<br>(Swing) → Presenter →<br>Service → DAO → BD.<br>Ninguna capa puede<br>saltarse otra.|0 llamadas directas desde UI<br>a DAO o BD|ArchUnit o<br>revisión manual<br>de dependencias|
|RNF<br>-04.2|Manejo de<br>excepciones|Las DAOException de la<br>capa DAO se transforman<br>en ServiceException antes<br>de propagarse. La capa UI<br>solo recibe<br>ServiceException.|0 <br>DAOException/SQLExceptio<br>n capturadas en la capa UI|Análisis estático<br>+ revisión de<br>código|
|RNF<br>-04.3|SRP|Cada clase tiene una<br>única razón para cambiar.<br>God classes y métodos de<br>más de 30 líneas son<br>señales de revisión<br>obligatoria.|Ninguna clase supera las 300<br>líneas; ningún método supera<br>30 líneas|SonarQube:<br>Cognitive<br>Complexity|
|RNF<br>-04.4|Clases<br>utilitarias|Las clases de utilidad<br>(Validators, Formatters,<br>Constants) deben ser final<br>con constructor privado.|0 instancias de clases<br>utilitarias creadas|Revisión de<br>código|
|RNF<br>-04.5|Cobertura<br>de pruebas|La lógica de negocio en la<br>capa Service debe tener<br>cobertura >= 80%. Las<br>DAOs se prueban con<br>tests de integración.|Coverage >= 80% en<br>paquete service.*|JaCoCo report<br>en pipeline CI|
|RNF<br>-04.6|Estilo de<br>código|El proyecto debe seguir<br>las convenciones Google<br>Java Style Guide. El<br>código debe pasar<br>Checkstyle sin errores.|0 violaciones Checkstyle en<br>el pipeline|Checkstyle<br>integrado en<br>Maven/Gradle<br>build|


## **4.5 Disponibilidad y Resiliencia — RNF-05**












|ID|Categoría|Descripción|Umbral|Verificación|
|---|---|---|---|---|
|RNF<br>-05.1|Manejo de<br>red|El sistema debe detectar errores<br>de conexión con Supabase y<br>mostrar un mensaje descriptivo<br>sin crashear ni mostrar<br>NullPointerException.|La aplicación no<br>lanza NPE ni cierra<br>abruptamente ante<br>pérdida de red|Test de<br>desconexión de<br>red durante<br>operación|
|RNF<br>-05.2|Cierre<br>limpio|Al cerrarse la aplicación<br>(WindowClosingEvent), debe<br>invocarse<br>ConnectionPool.close() para<br>liberar todas las conexiones del<br>pool.|0 conexiones<br>huérfanas en<br>HikariCP tras cerrar<br>la app|Monitoreo de<br>conexiones activas<br>en Supabase<br>Dashboard|



Compra Venta © Abril 2026  |  Página 26


**Documento Técnico v4.0**












|ID|Categoría|Descripción|Umbral|Verificación|
|---|---|---|---|---|
|RNF<br>-05.3|Timeout de<br>BD|Las operaciones de BD deben<br>tener un timeout máximo de 30<br>segundos. Superado ese límite,<br>se aborta la operación y se<br>informa al usuario.|0 operaciones<br>bloqueando la UI<br>indefinidamente|Test con query<br>lenta (pg_sleep) +<br>verificación de<br>timeout|


## **4.6 Observabilidad — RNF-06**












|ID|Categoría|Descripción|Umbral|Verificación|
|---|---|---|---|---|
|RNF<br>-06.1|Logging<br>estructurado|El sistema debe usar SLF4J<br>+ Logback con niveles:<br>ERROR (excepciones no<br>recuperables), WARN<br>(condiciones inesperadas),<br>INFO (operaciones clave).|Todos los flujos críticos<br>tienen al menos un log<br>INFO|Revisión de logs<br>durante pruebas<br>de integración|
|RNF<br>-06.2|Auditoría de<br>datos|Todas las operaciones de<br>escritura sobre pawns, sales<br>y articles deben registrarse<br>en audit_log con: tabla,<br>operación, record_id,<br>old_data (JSON) y new_data<br>(JSON).|100% de<br>INSERT/UPDATE/DELETE<br>en tablas críticas tienen<br>entrada en audit_log|Test de<br>integración:<br>verificar<br>audit_log tras<br>cada operación|
|RNF<br>-06.3|Sin stack<br>traces al<br>usuario|Las excepciones no<br>recuperables deben<br>loguearse con nivel ERROR<br>(con stack trace completo en<br>el log) y mostrar al usuario<br>solo un mensaje amigable.|0 stack traces en la<br>interfaz gráfica|Inyección de<br>error controlado<br>+ verificación<br>visual|


## **4.7 Facturación y Secuencias — RNF-07 (NUEVO)**












|ID|Categoría|Descripción|Umbral|Verificación|
|---|---|---|---|---|
|RNF<br>-07.1|Rendimiento<br>PDF|El sistema debe generar<br>facturas PDF en menos de 3<br>segundos para ventas con hasta<br>20 artículos.|p95 < 3s para hasta<br>20 artículos|Test de<br>generación con<br>JMH o<br>benchmarking<br>manual|
|RNF<br>-07.2|Gestión de<br>PDF|Las facturas generadas se<br>almacenan como bytes en<br>memoria durante la sesión y se<br>ofrecen para guardar/imprimir.<br>No se persisten<br>automáticamente en disco.|0 archivos PDF<br>creados<br>automáticamente en<br>disco sin acción del<br>usuario|Verificación de<br>filesystem post-<br>generación|
|RNF<br>-07.3|Unicidad de<br>factura|El número de factura es único y<br>secuencial por día. La<br>generación es thread-safe<br>mediante|0 colisiones en<br>número de factura<br>bajo carga<br>concurrente|Test de<br>concurrencia con<br>múltiples hilos<br>generando|



Compra Venta © Abril 2026  |  Página 27


**Documento Técnico v4.0**



|ID|Categoría|Descripción|Umbral|Verificación|
|---|---|---|---|---|
|||next_invoice_number() con<br>locking en BD.||facturas<br>simultáneamente|


Compra Venta © Abril 2026  |  Página 28




**Documento Técnico v4.0**

# **5. Diseño de Base de Datos — Modelo Relacional**

## **5.1 Descripción del Modelo y Decisiones de Diseño**
















|Tabla|Descripción|Relaciones Clave|Decisión de Diseño|
|---|---|---|---|
|profile|Usuarios del sistema.<br>Extiende auth.users de<br>Supabase Auth.|FK → auth.users(id)<br>ON DELETE<br>CASCADE|UUID como PK para alinear<br>con Supabase Auth. Trigger<br>on_auth_user_created la<br>puebla automáticamente.|
|clientes|Clientes que realizan<br>operaciones. Soft-<br>delete mediante status<br>ENUM.|Referenciada por<br>articles, pawns, sales<br>con ON DELETE<br>RESTRICT|ENUM cliente_status en lugar<br>de boolean para<br>extensibilidad. Índices GIN<br>pg_trgm para búsqueda<br>parcial eficiente.|
|articles|Inventario de artículos.<br>Estado derivado de<br>amount.|FK → clientes(id) ON<br>DELETE RESTRICT|Columna "sold" eliminada por<br>diseño. Estado calculado en<br>query: amount > 0 =<br>Disponible. Índice parcial<br>para artículos disponibles.|
|pawns|Empeños con 6<br>estados, cuotas y<br>peso.|FK → profile, articles,<br>clientes. ON DELETE<br>RESTRICT|ENUM pawn_status<br>reemplaza los booleans<br>expired/returned (P-02<br>resuelto). Columnas de<br>cuotas añadidas (P-01<br>resuelto). CHECK constraints<br>garantizan integridad.|
|pawn_payments|Registro individual de<br>pagos de cuotas<br>(NUEVA).|FK → pawns(id) ON<br>DELETE CASCADE|Tabla separada para historial<br>completo de pagos. Trigger<br>actualiza estado del empeño<br>automáticamente al insertar.|
|sales|Cabecera de ventas.|FK → profile, clientes.<br>Sales_details ON<br>DELETE CASCADE|Total calculado<br>dinámicamente con SUM<br>para evitar desnormalización.<br>Índice compuesto profile_id +<br>sale_date para historial del<br>empleado.|
|sales_details|Detalle de artículos<br>por venta.|FK → sales(id) ON<br>DELETE CASCADE,<br>articles(id)|UNIQUE(sale_id, article_id)<br>garantiza RF-05.1. unit_price<br>inmutable para preservar<br>precio histórico.|
|audit_log|Registro inmutable de<br>escrituras en tablas<br>críticas.|FK → profile(id) ON<br>DELETE SET NULL|BIGSERIAL como PK para<br>volumen alto. Sin UPDATE ni<br>DELETE (inmutabilidad). Solo<br>Admin puede leer (RLS).|
|invoice_sequence|Contador diario de<br>números de factura<br>(NUEVA).|Sin FK|ON CONFLICT DO UPDATE<br>para operación atómica y<br>thread-safe. Garantiza|



Compra Venta © Abril 2026  |  Página 29


**Documento Técnico v4.0**

|Tabla|Descripción|Relaciones Clave|Decisión de Diseño|
|---|---|---|---|
||||unicidad sin locks de<br>aplicación.|


## **5.2 Normalización y Justificación 3FN**


El modelo cumple la Tercera Forma Normal (3FN) por las siguientes razones:

   - profile: PK = id (UUID). Todos los atributos (email, full_name, rol, active) dependen exclusivamente del
UUID del usuario.

   - clientes: PK = id (SERIAL). email es UNIQUE pero no es PK; depende de id, no al revés.

   - articles: PK = id. Ningún atributo no-clave depende de otro no-clave. El estado ("Disponible/Sin stock") se
calcula en query para evitar desnormalización.

   - pawns: PK = id. installments_paid y installments_missed son atributos propios del empeño, no de article
ni de cliente.

   - pawn_payments: PK = id. amount_paid y payment_date son propios del pago individual. La actualización
de pawns se hace en trigger, no por almacenamiento redundante.

   - sales_details: unit_price se almacena intencionalmente como snapshot del precio al momento de la
venta (desnormalización controlada y justificada: el precio del artículo puede cambiar después).

## **5.3 Estrategia de Índices**
























|Índice|Tabla|Tipo|Justificación|
|---|---|---|---|
|idx_profile_full_name|profile|GIN (trgm)|Búsqueda parcial en listado de empleados|
|idx_clientes_last_name,<br>first_name, email|clientes|GIN (trgm)<br>× 3|RF-02.2: Búsqueda parcial case-insensitive<br>en 3 campos|
|idx_clientes_status|clientes|BTREE|Filtro empleados vs admin (status = Activo)|
|idx_articles_name|articles|GIN (trgm)|RF-03.2: Búsqueda parcial por nombre|
|idx_articles_category|articles|BTREE|Filtro por categoría en listado de inventario|
|idx_articles_available|articles|BTREE<br>parcial<br>(amount ><br>0)|Consulta frecuente: artículos disponibles<br>para empeño/venta|
|idx_pawns_status|pawns|BTREE|Filtros por estado en listado de empeños|
|idx_pawns_return|pawns|BTREE|fn_expire_overdue_pawns(): WHERE<br>return_date < hoy|
|idx_pawns_activos|pawns|BTREE<br>parcial<br>(status =<br>Activo)|Dashboard: COUNT y SUM de empeños<br>activos|
|idx_sales_profile_date|sales|BTREE<br>compuesto|RF-05.4: Historial de ventas por empleado y<br>fecha|
|idx_audit_changed_at|audit_log|BTREE<br>DESC|Consultas de auditoría ordenadas por fecha<br>reciente|



Compra Venta © Abril 2026  |  Página 30


**Documento Técnico v4.0**

## **5.4 Transacciones y Atomicidad**


**Empeños:** La reducción de stock se realiza en transacción JDBC (setAutoCommit(false)) que incluye tanto el
INSERT en pawns como el UPDATE en articles.amount. Si cualquier operación falla, se hace rollback completo.

**Ventas:** Se usa la stored procedure register_sale(p_profile_id, p_cliente_id, p_items JSONB) que ejecuta en una
única transacción PostgreSQL con lock pesimista FOR UPDATE para prevenir race conditions de stock. El
ERRCODE CV001 indica stock insuficiente y se captura con PSQLException.getSQLState() en Java.

**Pagos de cuotas:** El trigger update_pawn_status_on_payment() actualiza atómicamente installments_paid y el
estado del empeño en la misma transacción del INSERT en pawn_payments.

## **5.5 Row Level Security — Políticas por Tabla**

|Tabla|Política|Operación|Condición USING / WITH CHECK|
|---|---|---|---|
|pawns|pawns_admin_all|ALL|rol del solicitante = Admin|
|pawns|pawns_empleado_select|SELECT|rol del solicitante = Empleado (ve todos)|
|pawns|pawns_empleado_insert|INSERT|profile_id = auth.uid() AND rol = Empleado|
|pawns|pawns_empleado_update|UPDATE|profile_id = auth.uid() AND rol = Empleado|
|profile|profile_admin_all|ALL|rol del solicitante = Admin|
|profile|profile_own_select|SELECT|id = auth.uid()|
|audit_log|audit_admin_only|SELECT|rol del solicitante = Admin|



**Nota de arquitectura:** La capa de servicio Java aplica filtros adicionales como segunda línea de defensa (defensa en
profundidad). Esto garantiza que incluso si las políticas RLS tienen una brecha, la lógica de negocio previene accesos no
autorizados.


Compra Venta © Abril 2026  |  Página 31


**Documento Técnico v4.0**

# **6. Matriz de Trazabilidad — HU ↔ RF ↔ RNF ↔ Tablas de BD**


La siguiente matriz garantiza cobertura completa y permite detectar requisitos sin implementación o tablas sin
casos de uso asociados.











































































|HU|Descripción|RFs Asociados|RNFs Clave|Tablas / Objetos BD|Sprint|
|---|---|---|---|---|---|
|**HU-0**<br>**1**|Login con correo|RF-01.1, RF-01.2,<br>RF-01.3, RF-01.6|RNF-01.1,<br>RNF-01.2,<br>RNF-02.1|profile (SELECT), auth.users<br>(Supabase Auth)|S1|
|**HU-0**<br>**2**|Registro de<br>empleados|RF-01.4|RNF-01.1,<br>RNF-03.3|profile (INSERT via trigger),<br>auth.users|S1|
|**HU-0**<br>**3**|Cierre de sesión|RF-01.5|RNF-01.2|SessionManager (memoria),<br>Supabase Auth logout|S1|
|**HU-0**<br>**4**|Listado de clientes|RF-02.1, RF-02.2|RNF-02.1,<br>RNF-02.4,<br>RNF-03.2|clientes (SELECT), idx_clientes_*|S1|
|**HU-0**<br>**5**|Registrar cliente|RF-02.3, RF-02.4,<br>RF-02.5|RNF-03.3,<br>RNF-04.1|clientes (INSERT)|S1|
|**HU-0**<br>**6**|Editar cliente|RF-02.6|RNF-01.3,<br>RNF-03.3|clientes (UPDATE)|S2|
|**HU-0**<br>**7**|Eliminar / soft-<br>delete cliente|RF-02.6, RF-02.7|RNF-01.3,<br>RNF-03.4|clientes (DELETE o UPDATE<br>status)|S2|
|**HU-0**<br>**8**|Consultar<br>inventario|RF-03.1, RF-03.2,<br>RF-03.3|RNF-02.1,<br>RNF-02.4|articles (SELECT), idx_articles_*,<br>idx_articles_available|S1|
|**HU-0**<br>**9**|Registrar artículo|RF-03.4, RF-03.8|RNF-03.3,<br>RNF-04.1|articles (INSERT), clientes (FK)|S1|
|**HU-1**<br>**0**|Editar artículo +<br>auth precio|RF-03.5, RF-03.9|RNF-01.3|articles (UPDATE), audit_log<br>(INSERT), auth.users (validación)|S2|
|**HU-1**<br>**1**|Aumentar stock|RF-03.6|RNF-06.2|articles (UPDATE amount),<br>audit_log (INSERT)|S2|
|**HU-1**<br>**2**|Registrar empeño|RF-04.3, RF-04.4,<br>RF-03.7, RF-04.13|RNF-01.3,<br>RNF-02.3,<br>RNF-04.2|pawns (INSERT), articles<br>(UPDATE amount), audit_log,<br>trigger<br>validate_pawn_jewelry_weight|S1|
|**HU-1**<br>**3**|Listado empeños<br>por estado|RF-04.1, RF-04.2|RNF-01.5,<br>RNF-02.4|pawns (SELECT),<br>idx_pawns_status, RLS policies|S1|
|**HU-1**<br>**4**|Marcar empeño<br>devuelto|RF-04.5|RNF-06.2|pawns (UPDATE status =<br>Retirado), audit_log (INSERT)|S2|
|**HU-1**<br>**5**|Expiración de<br>empeños|RF-04.6, RF-04.7,<br>RF-04.8|RNF-02.1,<br>RNF-06.2|fn_expire_overdue_pawns(),<br>pawns (UPDATE), audit_log<br>(INSERT), idx_pawns_activos|S2|
|**HU-1**<br>**6**|Registrar pago de<br>cuota|RF-04.10, RF-04.11|RNF-06.2|pawn_payments (INSERT),<br>pawns (UPDATE via trigger),<br>audit_log|S2|
|**HU-1**<br>**7**|Registrar cuota<br>impagada|RF-04.12|RNF-06.2|fn_mark_pawn_lost(), pawns<br>(UPDATE), audit_log|S2|
|**HU-1**|Eliminar empeño|RF-04.8|RNF-01.3,|pawns (DELETE), audit_log|S3|


Compra Venta © Abril 2026  |  Página 32


**Documento Técnico v4.0**




































|HU|Descripción|RFs Asociados|RNFs Clave|Tablas / Objetos BD|Sprint|
|---|---|---|---|---|---|
|**8**|||RNF-03.4,<br>RNF-06.2|(INSERT con old_data)||
|**HU-1**<br>**9**|Registro atómico<br>de venta|RF-05.1, RF-05.2,<br>RF-05.3, RF-03.7|RNF-02.3,<br>RNF-04.2,<br>RNF-06.2|register_sale() SP, sales<br>(INSERT), sales_details<br>(INSERT), articles (UPDATE),<br>audit_log|S2|
|**HU-2**<br>**0**|Historial de ventas|RF-05.4, RF-05.5|RNF-02.4|sales (SELECT), sales_details<br>(JOIN SUM),<br>idx_sales_profile_date|S3|
|**HU-2**<br>**1**|Eliminar venta|RF-05.6|RNF-01.3,<br>RNF-03.4,<br>RNF-06.2|sales (DELETE CASCADE a<br>sales_details), audit_log|S3|
|**HU-2**<br>**2**|Gestión de<br>empleados|RF-06.1, RF-06.2|RNF-01.3|profile (SELECT, UPDATE<br>active)|S2|
|**HU-2**<br>**3**|Dashboard<br>ejecutivo|RF-07.1, RF-07.2,<br>RF-07.3|RNF-02.5,<br>RNF-03.2|v_dashboard (VIEW),<br>idx_pawns_activos,<br>idx_clientes_status|S1|
|**HU-2**<br>**4**|Generar factura<br>PDF|RF-08.1, RF-08.2,<br>RF-08.3|RNF-07.1,<br>RNF-07.2,<br>RNF-07.3|invoice_sequence (INSERT ON<br>CONFLICT),<br>next_invoice_number() función|S2|



Compra Venta © Abril 2026  |  Página 33


**Documento Técnico v4.0**

# **7. Arquitectura del Sistema**

## **7.1 Capas y Responsabilidades**













|Capa|Paquete Java|Responsabilidad|Tecnología|
|---|---|---|---|
|Presentación<br>(UI)|ui.views,<br>ui.components|Renderizar formularios y tablas.<br>Capturar eventos. Mostrar datos<br>formateados. Sin lógica de negocio.|Java Swing (JFrame,<br>JPanel, JTable,<br>SwingWorker)|
|Presenter /<br>ViewModel|ui.presenters|Coordinar flujo entre UI y Services.<br>Transformar DTOs. Gestionar<br>estado de formularios.|Java puro (Patrón<br>MVP)|
|Servicio<br>(Negocio)|service|Toda la lógica de negocio:<br>validaciones, reglas, orquestación<br>de DAOs, transacciones. Lanza<br>ServiceException.|Java 21 (interfaces +<br>implementaciones)|
|Acceso a<br>Datos (DAO)|dao|Ejecutar SQL mediante<br>PreparedStatement. Mapear<br>ResultSet a entidades. Gestionar<br>transacciones JDBC. Lanza<br>DAOException.|JDBC + HikariCP|
|Infraestructura|infrastructure|Configuración de conexión,<br>SessionManager, generadores de<br>PDF, constantes globales,<br>AppConfig.|HikariCP, iText 7,<br>SLF4J + Logback,<br>dotenv-java|
|Base de<br>Datos|—|Persistencia relacional. RLS,<br>triggers, stored procedures, vistas.|PostgreSQL 15<br>(Supabase)|

## **7.2 Patrones de Diseño Aplicados**



















|Patrón|Aplicación|Beneficio|
|---|---|---|
|Singleton (thread-<br>safe, DCL)|SessionManager,<br>DatabaseConnectionPool|Una única instancia de sesión y pool de<br>conexiones, segura ante concurrencia de<br>EDT/SwingWorker|
|Repository / DAO|ClienteDAO, ArticleDAO,<br>PawnDAO, SaleDAO, etc.|Aísla el acceso a datos, facilita testing con<br>mocks de Mockito|
|Presenter (MVP)|LoginPresenter,<br>DashboardPresenter,<br>ClientePresenter, etc.|Lógica de presentación testeable sin<br>instanciar componentes Swing|
|Strategy|InvoiceGenerator (iText o<br>PDFBox intercambiables)|Cambiar implementación de PDF sin<br>modificar servicios ni UI|
|Observer / Event|SwingWorker +<br>PropertyChangeListener|Actualización reactiva de UI sin bloquear el<br>EDT|
|Factory Method|PawnStatusFactory,|Creación de objetos desacoplada del tipo|


Compra Venta © Abril 2026  |  Página 34


**Documento Técnico v4.0**










|Patrón|Aplicación|Beneficio|
|---|---|---|
||InvoiceFactory|concreto|
|Builder|InvoiceData.builder() para<br>construcción de facturas PDF|Construcción compleja paso a paso con<br>objetos inmutables|
|Command<br>(Auditoría)|AuditEntry encapsula cada<br>operación de escritura|Registro uniforme y desacoplado de todas las<br>operaciones|


## **7.3 Dependencias del Proyecto (pom.xml)**







|Dependencia|Versión|Propósito|Licencia|
|---|---|---|---|
|HikariCP|5.1.0|Pool de conexiones JDBC de alto<br>rendimiento|Apache 2.0|
|PostgreSQL JDBC<br>Driver|42.7.1|Driver para conectar con<br>Supabase/PostgreSQL|BSD-2|
|iText 7 Core (o<br>Apache PDFBox)|7.2.5 /<br>3.0.x|Generación de facturas PDF|AGPL/Comercial o<br>Apache 2.0|
|SLF4J + Logback|2.0.x|Logging estructurado por niveles|MIT / EPL|
|dotenv-java<br>(cdimascio)|3.0.0|Carga de variables de entorno<br>desde .env|Apache 2.0|
|JUnit 5|5.10.1|Framework de pruebas unitarias|EPL 2.0|
|Mockito Core|5.8.0|Mocking para pruebas unitarias de<br>servicios|MIT|
|JaCoCo|0.8.11|Cobertura de código >= 80%<br>(RNF-04.5)|EPL 2.0|


Compra Venta © Abril 2026  |  Página 35


**Documento Técnico v4.0**

# **8. Checklist de Implementación por Sprint**

## **Sprint 1 — MVP Core (Prioridad Alta)**































|#|Tarea|Módulo|HU/RF Base|
|---|---|---|---|
|1|Configurar proyecto Maven con todas<br>las dependencias del pom.xml|Infraestructura|—|
|2|Implementar DatabaseConnectionPool<br>(HikariCP) con variables de entorno<br>desde .env|Infraestructura|RNF-01.4, RNF-02.2|
|3|Implementar SessionManager<br>Singleton thread-safe (volatile + DCL)|Infraestructura|RF-01.6|
|4|Ejecutar DDL completo v3.0 en<br>Supabase SQL Editor|Base de datos|Todas las HU|
|5|Configurar RLS en Supabase (pawns,<br>profile, audit_log)|Base de datos|RNF-01.5|
|6|Implementar AuthService (login, logout,<br>validación de active)|Autenticación|HU-01, HU-03, RF-01.1 a RF-01.6|
|7|Implementar LoginView +<br>LoginPresenter|UI<br>Autenticación|HU-01|
|8|Implementar DashboardView con<br>consulta a v_dashboard (SwingWorker)|Dashboard|HU-23|
|9|Implementar ClienteDAO +<br>ClienteService + ClienteView (listado +<br>registro)|Clientes|HU-04, HU-05|
|10|Implementar ArticleDAO +<br>ArticleService + ArticuloView (listado +<br>registro)|Inventario|HU-08, HU-09|
|11|Implementar PawnDAO + PawnService<br>+ EmpeñoView (registro + listado)|Empeños|HU-12, HU-13|
|12|Implementar<br>fn_expire_overdue_pawns() ejecutada<br>al iniciar en SwingWorker|Empeños|HU-15, RF-04.6|
|13|Tests unitarios: AuthService,<br>ClienteService, ArticleService (>=80%)|Testing|RNF-04.5|

## **Sprint 2 — Funcionalidades Avanzadas**

|#|Tarea|Módulo|HU/RF Base|
|---|---|---|---|
|14|Implementar VentaService + stored<br>procedure register_sale()|Ventas|HU-19, RF-05.1 a RF-05.3|



Compra Venta © Abril 2026  |  Página 36


**Documento Técnico v4.0**















|#|Tarea|Módulo|HU/RF Base|
|---|---|---|---|
|15|Implementar VentaView con soporte<br>multi-artículo|Ventas|HU-19|
|16|Implementar módulo de pagos de<br>cuotas (pawn_payments + triggers)|Empeños|HU-16, RF-04.10, RF-04.11|
|17|Implementar fn_mark_pawn_lost() y<br>lógica de cuotas impagadas|Empeños|HU-17, RF-04.12|
|18|Implementar modal de autorización<br>temporal de Admin para cambio de<br>precio|Inventario|HU-10, RF-03.9|
|19|Implementar ITextInvoiceGenerator +<br>FacturaService + diálogo de factura|Facturación|HU-24, RF-08.1 a RF-08.3|
|20|Implementar EmpleadoView (listado y<br>activar/desactivar por Admin)|Empleados|HU-22, RF-06.1, RF-06.2|
|21|Implementar soft-delete de clientes por<br>Empleado|Clientes|HU-07, RNF-08.1|
|22|Implementar edición de clientes y<br>artículos con validaciones|Clientes /<br>Inventario|HU-06, HU-10|
|23|Implementar ajuste de stock con<br>registro en audit_log|Inventario|HU-11, RF-03.6|
|24|Tests unitarios: VentaService,<br>PawnService (cuotas, estados)|Testing|RNF-04.5|

## **Sprint 3 — Refinamiento y Calidad**

|#|Tarea|Módulo|HU/RF Base|
|---|---|---|---|
|25|Historial de ventas con filtros (fecha,<br>cliente, empleado)|Ventas|HU-20, RF-05.4, RF-05.5|
|26|Eliminación de ventas con audit_log<br>(solo Admin)|Ventas|HU-21, RF-05.6|
|27|Eliminación de empeños con audit_log<br>(solo Admin)|Empeños|HU-18, RF-04.8|
|28|Cobertura JaCoCo >= 80% en paquete<br>service.*|Testing|RNF-04.5|
|29|Configurar Checkstyle (Google Java<br>Style Guide) en pipeline Maven|Calidad|RNF-04.6|
|30|Tests de integración para DAOs con<br>BD de prueba (schema separado)|Testing|RNF-04.5|
|31|Pruebas de penetración de capa de|Seguridad|RNF-01.3|



Compra Venta © Abril 2026  |  Página 37


**Documento Técnico v4.0**









|#|Tarea|Módulo|HU/RF Base|
|---|---|---|---|
||servicio (simulación roles incorrectos)|||
|32|Revisión final de todos los mensajes de<br>error (español, sin stack traces)|UX|RNF-03.1, RNF-06.3|
|33|Test de cierre limpio:<br>ConnectionPool.close() en<br>WindowClosingEvent|Resiliencia|RNF-05.2|
|34|Test de timeout de BD con pg_sleep y<br>verificación de timeout HikariCP|Resiliencia|RNF-05.3|



_— Fin del Documento_


Compra Venta © Abril 2026  |  Página 38


