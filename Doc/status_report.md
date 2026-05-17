# Reporte de Estado del Proyecto CompraVenta

Basado en la documentación v5, v5.1 y v6, a continuación se presenta el estado actual del desarrollo.

## ✅ Funcionalidades LISTAS (Implementadas)

| Módulo | Funcionalidad | Descripción Técnica |
| :--- | :--- | :--- |
| **Base de Datos** | Infraestructura v6.1 | Esquema completo con Triggers, Procedimientos Almacenados y Auditoría. |
| **Seguridad** | Empleado Automático | Creación automática de registro en `employees` al registrarse en Supabase Auth. |
| **Seguridad** | Gestión de Sesión | `SessionManager` thread-safe para manejo de tokens JWT y UUID de empleados. |
| **Ventas** | Venta Atómica | Uso de `register_sale()` (PostgreSQL) para asegurar integridad de stock. |
| **Ventas** | Ventas Anónimas | Soporte para campo `p_nombre_anon` cuando no hay cliente registrado. |
| **Inventario** | Modelos Extendidos | `Article` ya soporta `source_type`, `item_state` y `purchase_price`. |
| **Inventario** | Categorización | Enums alineados: Electrodomésticos, Joyería, Herramientas, Tecnología, Otro. |
| **Empeños** | Seguimiento de Cuotas | Columnas `installments_paid` y `installments_missed` operativas en `pawns`. |
| **Dashboard** | Vista de Métricas | `v_dashboard` lista para consumo con indicadores clave en tiempo real. |

---

## 🛠️ Funcionalidades PENDIENTES (Por Desarrollar)

| Módulo | Funcionalidad | Detalle del Requisito |
| :--- | :--- | :--- |
| **Seguridad** | Selección de Rol | ~~**El Admin debe poder seleccionar el rol (Admin o Empleado) al momento del registro** (Actualmente hardcodeado como 'Empleado').~~ <br> <ins>**[CORREGIDO]:** Implementado en `EmployeeRegisterDialog.java` con selector dinámico de rol (`cmbRole`).</ins> |
| **Compras** | Módulo de Compras | ~~Implementación de `PurchaseService` y `PurchaseDao` para adquisiciones directas.~~ <br> <ins>**[CORREGIDO]:** Implementado exitosamente con `PurchaseService`, `PurchaseDao`, `PurchasePanel` y `PurchaseDialog`.</ins> |
| **Facturación** | Generación de PDF | ~~Integración de iText/PDFBox para emitir comprobantes de venta y empeño.~~ <br> <ins>**[CORREGIDO]:** Implementado en `PdfInvoiceGenerator` usando iText7 para emitir facturas y boletas en PDF.</ins> |
| **Clientes** | Registro Rápido | ~~Modal simplificado (Nombre + Teléfono) para uso durante operaciones en mostrador.~~ <br> <ins>**[CORREGIDO]:** Implementado en `ClienteDialog` flexibilizando validaciones (DNI y Apellido opcionales).</ins> |
| **Empeños** | Pantalla Única | ~~Vista unificada que permita crear Cliente + Artículo + Empeño en un solo flujo.~~ <br> <ins>**[CORREGIDO]:** Implementado en `PawnDialog` con transacción atómica ágil (`registerAgilePawn`).</ins> |
| **Ventas** | Venta Rápida UX | Pantalla optimizada para mostrador con buscador instantáneo y atajos de teclado. <br> **[PENDIENTE]:** Falta implementar búsqueda instantánea con Debounce (300ms) y atajos de teclado en mostrador. |
| **Automatización** | Expiración en Inicio | ~~`SwingWorker` para ejecutar limpieza de empeños vencidos al abrir la aplicación.~~ <br> <ins>**[CORREGIDO]:** Implementado en `App.java` ejecutando limpieza asíncrona de empeños vencidos al iniciar.</ins> |
| **Auditoría** | Logging Local | ~~Implementación de SLF4J/Log4j2 para auditoría técnica en archivos locales.~~ <br> <ins>**[CORREGIDO]:** Implementado con `logback.xml` guardando historial y errores en `logs/compraventa.log`.</ins> |
