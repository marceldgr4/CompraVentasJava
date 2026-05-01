# Revisión del Proyecto CompraVenta

## 1. Falta por Implementar (Missing Implementations)

1. **Módulo de Facturación PDF (HU-24 & RF-08):**
   - No hay rastro de la integración con librerías como `iText 7` o `Apache PDFBox`.
   - Las clases requeridas para la generación como `InvoiceGenerator` no existen en la capa `Utils` o `Service`.
   - Falta el manejo de la secuencia para numeración única de facturas en base de datos (`invoice_sequence`, RF-08.2).
2. **Expiración automática de Empeños al inicio (HU-15 & RF-04.6):**
   - El documento de especificaciones (`v4.md`) indica que la función de base de datos `fn_expire_overdue_pawns()` debe ejecutarse en un hilo secundario (`SwingWorker`) al iniciar la aplicación.
   - Actualmente, `App.java` no realiza esta llamada. El método respectivo existe en `PawnDao` y `PawnService` pero nunca se invoca automáticamente al arranque del sistema.
3. **Carga Óptima del Dashboard (HU-23 & RF-07.1):**
   - En `DashboardPanel.java`, los KPIs se están calculando trayendo todas las listas completas desde la base de datos hacia la memoria de la aplicación, y calculando su tamaño (Ej: `pawnService.getActivePawns().size()`).
   - La documentación especifica expresamente que debe usar la vista `v_dashboard` con una sola consulta SQL para optimizar rendimiento. Extraer toda la data para contarla es un fallo crítico de arquitectura y rendimiento.

## 2. Fallos, Errores y Bugs Detectados

1. **Credenciales en Texto Plano (Violación RNF-01.4):**
   - En el archivo `TestDb.java` las credenciales de conexión (`url`, `user`, `pass`) están quemadas en el código fuente (hardcoded). Esto representa una vulnerabilidad de seguridad que contradice la directiva de usar el archivo `.env`.
2. **Excepciones Silenciadas o Mal Manejadas (Violación RNF-04.2 & RNF-06.3):**
   - En `SalePanel.java` (línea 169) y `App.java` (línea 14): `} catch (Exception ignored) {}`. Se están ignorando errores críticos (como fallos al parsear fechas al tratar de filtrar las ventas) que causan un comportamiento errático en la UI sin dejar rastro en logs.
   - En `DashboardPanel.java` (línea 164): Se utiliza `e.printStackTrace();` atrapando una excepción genérica `Exception`, en lugar de usar un logger estructurado (SLF4J/Logback) y mostrar un mensaje amigable en pantalla para notificar que el Dashboard falló en cargar.
3. **Ausencia de Componentes Relacionados a "pawn_payments" en Servicios:**
   - La base de datos tiene la tabla y hay un `PawnPaymentDao.java`, pero faltan paneles (UI) y diálogos adecuados que expongan la opción "Registrar pago de cuota de empeño" de forma robusta e integrada como indica la HU-16.

## 3. Recomendaciones y Correcciones a Seguir

1. **Refactorizar `DashboardPanel.java` y Capa de Datos:** 
   - Eliminar el uso de `.size()` en listas inmensas. Crear un método en un nuevo `DashboardDao.java` que llame a `SELECT * FROM v_dashboard` y retorne un objeto `DashboardDTO` consolidado.
2. **Crear el Módulo de Facturas (PDF):** 
   - Agregar la dependencia en el `pom.xml` (`iText` o `PDFBox`).
   - Codificar `InvoiceGenerator` y conectarlo como acción final luego de completar una Venta o un Empeño de forma exitosa.
3. **Corregir Inicialización en `App.java`:** 
   - Inyectar o instanciar `PawnService` e invocar `expireOverduePawns()` dentro de un hilo secundario antes o justo después de lanzar el `LoginFrame`.
4. **Implementar SLF4J + Logback y Borrar Stacktraces Vistos:** 
   - Erradicar `e.printStackTrace()` del código y reemplazar los bloques `catch (Exception ignored)` vacíos. El sistema debe lanzar `showError()` en los paneles o almacenar alertas en un Logger.
5. **Asegurar Archivos Sensibles:** 
   - Borrar `TestDb.java` si ya no es necesario, o cambiar sus variables internas a una lectura por variable de entorno/`.env`.
