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
| **Seguridad** | Selección de Rol | **El Admin debe poder seleccionar el rol (Admin o Empleado) al momento del registro** (Actualmente hardcodeado como 'Empleado'). |
| **Compras** | Módulo de Compras | Implementación de `PurchaseService` y `PurchaseDao` para adquisiciones directas. |
| **Facturación** | Generación de PDF | Integración de iText/PDFBox para emitir comprobantes de venta y empeño. |
| **Clientes** | Registro Rápido | Modal simplificado (Nombre + Teléfono) para uso durante operaciones en mostrador. |
| **Empeños** | Pantalla Única | Vista unificada que permita crear Cliente + Artículo + Empeño en un solo flujo. |
| **Ventas** | Venta Rápida UX | Pantalla optimizada para mostrador con buscador instantáneo y atajos de teclado. |
| **Automatización** | Expiración en Inicio | `SwingWorker` para ejecutar limpieza de empeños vencidos al abrir la aplicación. |
| **Auditoría** | Logging Local | Implementación de SLF4J/Log4j2 para auditoría técnica en archivos locales. |
