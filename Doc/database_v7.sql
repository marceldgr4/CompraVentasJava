-- ============================================================
-- DDL COMPRAVENTA v7.0.0 — CONSOLIDADO Y PROFESIONAL
-- Compatible: PostgreSQL 16 / Supabase
-- Autor: Sistema de Casa de Empeños
-- Fecha: 2026-05-17
-- ============================================================
--
-- CAMBIOS PRINCIPALES EN v7.0.0:
-- - Renombrado: profile → employees (claridad semántica)
-- - Unificación: todas las tablas ahora usan employee_id en lugar de profile_id
-- - Índices optimizados para consultas frecuentes
-- - Políticas RLS más explícitas y seguras
-- - Funciones con mejor manejo de errores y validaciones
-- - Eliminación de inconsistencias entre documentos fuente
-- - Documentación inline mejorada
--
-- ============================================================

-- ============================================================
-- 0. LIMPIEZA TOTAL (Idempotencia)
-- ============================================================

-- Triggers
DROP TRIGGER IF EXISTS on_auth_user_created       ON auth.users;
DROP TRIGGER IF EXISTS trg_employee_upd           ON public.employees;
DROP TRIGGER IF EXISTS trg_profile_upd            ON public.employees;
DROP TRIGGER IF EXISTS trg_cliente_upd            ON public.clientes;
DROP TRIGGER IF EXISTS trg_article_upd            ON public.articles;
DROP TRIGGER IF EXISTS trg_pawn_upd               ON public.pawns;
DROP TRIGGER IF EXISTS trg_audit_articles         ON public.articles;
DROP TRIGGER IF EXISTS trg_audit_pawns            ON public.pawns;
DROP TRIGGER IF EXISTS trg_audit_sales            ON public.sales;
DROP TRIGGER IF EXISTS trg_audit_purchases        ON public.purchases;
DROP TRIGGER IF EXISTS trg_pawn_payment_after     ON public.pawn_payments;

-- Vistas y Funciones
DROP VIEW     IF EXISTS public.v_dashboard                                    CASCADE;
DROP FUNCTION IF EXISTS public.fn_expire_overdue_pawns()                      CASCADE;
DROP FUNCTION IF EXISTS public.register_sale(UUID, INTEGER, JSONB, TEXT)      CASCADE;
DROP FUNCTION IF EXISTS public.fn_mark_pawn_lost(INTEGER)                     CASCADE;
DROP FUNCTION IF EXISTS public.handle_new_user()                              CASCADE;
DROP FUNCTION IF EXISTS public.set_updated_at()                               CASCADE;
DROP FUNCTION IF EXISTS public.update_pawn_status_on_payment()                CASCADE;
DROP FUNCTION IF EXISTS public.validate_pawn_jewelry_weight()                 CASCADE;
DROP FUNCTION IF EXISTS public.next_invoice_number()                          CASCADE;
DROP FUNCTION IF EXISTS public.fn_audit_trigger()                             CASCADE;
DROP FUNCTION IF EXISTS public.get_my_role()                                  CASCADE;

-- Tablas (en orden inverso de dependencias)
DROP TABLE IF EXISTS public.invoice_sequence  CASCADE;
DROP TABLE IF EXISTS public.audit_log         CASCADE;
DROP TABLE IF EXISTS public.sales_details     CASCADE;
DROP TABLE IF EXISTS public.sales             CASCADE;
DROP TABLE IF EXISTS public.purchases         CASCADE;
DROP TABLE IF EXISTS public.pawn_payments     CASCADE;
DROP TABLE IF EXISTS public.pawns             CASCADE;
DROP TABLE IF EXISTS public.articles          CASCADE;
DROP TABLE IF EXISTS public.employees         CASCADE;
DROP TABLE IF EXISTS public.profile           CASCADE; -- Por si aún existe el nombre viejo
DROP TABLE IF EXISTS public.clientes          CASCADE;

-- Tipos enumerados
DROP TYPE IF EXISTS public.role_user         CASCADE;
DROP TYPE IF EXISTS public.article_category  CASCADE;
DROP TYPE IF EXISTS public.cliente_status    CASCADE;
DROP TYPE IF EXISTS public.pawn_status       CASCADE;
DROP TYPE IF EXISTS public.registration_type CASCADE;
DROP TYPE IF EXISTS public.source_type       CASCADE;
DROP TYPE IF EXISTS public.item_state        CASCADE;

-- ============================================================
-- 1. EXTENSIONES REQUERIDAS
-- ============================================================
CREATE EXTENSION IF NOT EXISTS pg_trgm;       -- Búsqueda de texto difusa
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";   -- Generación de UUIDs

-- ============================================================
-- 2. TIPOS ENUMERADOS
-- ============================================================

-- Roles de usuario en el sistema
CREATE TYPE public.role_user AS ENUM (
    'Admin',    -- Administrador con permisos completos
    'Empleado'  -- Empleado con permisos estándar
);

-- Categorías de artículos en inventario
CREATE TYPE public.article_category AS ENUM (
    'Electrodomesticos',
    'Joyeria',
    'Herramientas',
    'Tecnologia',
    'Otro'
);

-- Estados del cliente
CREATE TYPE public.cliente_status AS ENUM (
    'Activo',    -- Cliente activo en el sistema
    'Eliminado'  -- Cliente marcado como eliminado (soft delete)
);

-- Estados del empeño
CREATE TYPE public.pawn_status AS ENUM (
    'Activo',      -- Empeño activo, en período vigente
    'Vencido',     -- Empeño vencido, superó fecha de retorno
    'Finalizado',  -- Empeño finalizado, todas las cuotas pagadas
    'Perdido',     -- Empeño perdido, artículo pasa a inventario
    'Retirado',    -- Empeño retirado por el cliente
    'Vendido'      -- Artículo empeñado fue vendido
);

-- Tipo de registro del cliente
CREATE TYPE public.registration_type AS ENUM (
    'RAPIDO',     -- Registro rápido con datos mínimos
    'COMPLETO'    -- Registro completo con todos los datos
);

-- Origen del artículo en inventario
CREATE TYPE public.source_type AS ENUM (
    'EMPENO',  -- Artículo proviene de un empeño perdido
    'COMPRA',  -- Artículo comprado directamente
    'AJUSTE',  -- Artículo por ajuste de inventario
    'OTRO'     -- Otro origen
);

-- Estado físico del artículo
CREATE TYPE public.item_state AS ENUM (
    'Nuevo',
    'Bueno',
    'Regular',
    'Dañado'
);

-- ============================================================
-- 3. TABLAS MAESTRAS
-- ============================================================

-- Tabla de empleados del sistema (usuarios autenticados)
CREATE TABLE public.employees (
    id          UUID                 PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email       VARCHAR(255)         NOT NULL UNIQUE,
    full_name   VARCHAR(255)         NOT NULL,
    rol         public.role_user     NOT NULL DEFAULT 'Empleado',
    active      BOOLEAN              NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ          NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  public.employees IS 'Empleados del sistema sincronizados con auth.users';
COMMENT ON COLUMN public.employees.id IS 'UUID del usuario en auth.users';
COMMENT ON COLUMN public.employees.active IS 'Indica si el empleado puede acceder al sistema';
COMMENT ON COLUMN public.employees.rol IS 'Rol del empleado: Admin o Empleado';

-- Tabla de clientes (personas que empeñan, compran o venden)
CREATE TABLE public.clientes (
    id                SERIAL                      PRIMARY KEY,
    first_name        VARCHAR(255)                NOT NULL,
    last_name         VARCHAR(255),
    cedula            VARCHAR(50)                 UNIQUE 
                      CONSTRAINT chk_cedula_numeric CHECK (cedula ~ '^[0-9]+$'),
    email             VARCHAR(255)                UNIQUE,
    phone             VARCHAR(50)                 
                      CONSTRAINT chk_phone_format CHECK (phone ~ '^[+]?[0-9]{7,15}$'),
    address           TEXT,
    city              VARCHAR(100),
    registration_type public.registration_type    NOT NULL DEFAULT 'COMPLETO',
    status            public.cliente_status       NOT NULL DEFAULT 'Activo',
    created_at        TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ                 NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  public.clientes IS 'Clientes del sistema (compradores, vendedores, personas que empeñan)';
COMMENT ON COLUMN public.clientes.registration_type IS 'RAPIDO permite crear clientes sin todos los datos, COMPLETO requiere datos completos';
COMMENT ON COLUMN public.clientes.status IS 'Activo o Eliminado (soft delete)';

-- ============================================================
-- 4. INVENTARIO
-- ============================================================

-- Tabla de artículos en inventario
CREATE TABLE public.articles (
    id             SERIAL                  PRIMARY KEY,
    cliente_id     INTEGER                 REFERENCES public.clientes(id) ON DELETE SET NULL,
    name_article   VARCHAR(255)            NOT NULL,
    description    TEXT,
    category       public.article_category NOT NULL DEFAULT 'Otro',
    source_type    public.source_type      NOT NULL DEFAULT 'OTRO',
    item_state     public.item_state       NOT NULL DEFAULT 'Bueno',
    amount         INTEGER                 NOT NULL DEFAULT 0 CHECK (amount >= 0),
    price          NUMERIC(12,2)           NOT NULL CHECK (price >= 0),
    purchase_price NUMERIC(12,2)           CHECK (purchase_price >= 0),
    created_at     TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ             NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  public.articles IS 'Inventario de artículos disponibles para la venta';
COMMENT ON COLUMN public.articles.cliente_id IS 'Cliente que vendió el artículo (si aplica)';
COMMENT ON COLUMN public.articles.source_type IS 'Origen del artículo: EMPENO, COMPRA, AJUSTE, OTRO';
COMMENT ON COLUMN public.articles.amount IS 'Cantidad disponible en stock';
COMMENT ON COLUMN public.articles.price IS 'Precio de venta al público';
COMMENT ON COLUMN public.articles.purchase_price IS 'Precio al que se compró (si aplica)';

-- ============================================================
-- 5. COMPRAS A CLIENTES
-- ============================================================

-- Tabla de compras realizadas a clientes
CREATE TABLE public.purchases (
    id             SERIAL        PRIMARY KEY,
    employee_id    UUID          NOT NULL REFERENCES public.employees(id),
    cliente_id     INTEGER       NOT NULL REFERENCES public.clientes(id),
    article_id     INTEGER       NOT NULL REFERENCES public.articles(id),
    purchase_price NUMERIC(12,2) NOT NULL CHECK (purchase_price > 0),
    purchase_date  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    notes          TEXT
);

COMMENT ON TABLE  public.purchases IS 'Registro de compras de artículos a clientes';
COMMENT ON COLUMN public.purchases.employee_id IS 'Empleado que registró la compra';
COMMENT ON COLUMN public.purchases.purchase_price IS 'Precio pagado al cliente por el artículo';

-- ============================================================
-- 6. EMPEÑOS
-- ============================================================

-- Tabla principal de empeños
CREATE TABLE public.pawns (
    id                  SERIAL             PRIMARY KEY,
    employee_id         UUID               NOT NULL REFERENCES public.employees(id),
    article_id          INTEGER            NOT NULL REFERENCES public.articles(id),
    cliente_id          INTEGER            NOT NULL REFERENCES public.clientes(id),
    amount              INTEGER            NOT NULL CHECK (amount > 0),
    price               NUMERIC(12,2)      NOT NULL CHECK (price > 0),
    weight_grams        NUMERIC(8,2),
    installment_count   INTEGER            NOT NULL DEFAULT 1 CHECK (installment_count >= 1),
    installments_paid   INTEGER            NOT NULL DEFAULT 0 CHECK (installments_paid >= 0),
    installments_missed INTEGER            NOT NULL DEFAULT 0 CHECK (installments_missed >= 0),
    pawn_date           DATE               NOT NULL DEFAULT CURRENT_DATE,
    return_date         DATE               NOT NULL,
    status              public.pawn_status NOT NULL DEFAULT 'Activo',
    notes               TEXT,
    updated_at          TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_return_after_pawn CHECK (return_date > pawn_date),
    CONSTRAINT chk_paid_lte_total    CHECK (installments_paid <= installment_count)
);

COMMENT ON TABLE  public.pawns IS 'Empeños de artículos por parte de clientes';
COMMENT ON COLUMN public.pawns.employee_id IS 'Empleado que registró el empeño';
COMMENT ON COLUMN public.pawns.price IS 'Monto total prestado al cliente';
COMMENT ON COLUMN public.pawns.weight_grams IS 'Peso en gramos (para joyería)';
COMMENT ON COLUMN public.pawns.installment_count IS 'Número total de cuotas acordadas';
COMMENT ON COLUMN public.pawns.installments_paid IS 'Cuotas ya pagadas';
COMMENT ON COLUMN public.pawns.installments_missed IS 'Cuotas perdidas consecutivas';

-- Tabla de pagos de cuotas de empeño
CREATE TABLE public.pawn_payments (
    id                     SERIAL        PRIMARY KEY,
    pawn_id                INTEGER       NOT NULL REFERENCES public.pawns(id) ON DELETE CASCADE,
    amount                 NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    payment_date           DATE          NOT NULL DEFAULT CURRENT_DATE,
    notes                  VARCHAR(500),
    created_by_employee_id UUID          NOT NULL REFERENCES public.employees(id),
    is_missed              BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  public.pawn_payments IS 'Registro de pagos de cuotas de empeños';
COMMENT ON COLUMN public.pawn_payments.is_missed IS 'TRUE si es un registro de cuota perdida, FALSE si es pago real';

-- ============================================================
-- 7. VENTAS
-- ============================================================

-- Tabla de ventas (cabecera)
CREATE TABLE public.sales (
    id                  SERIAL      PRIMARY KEY,
    employee_id         UUID        NOT NULL REFERENCES public.employees(id),
    cliente_id          INTEGER     REFERENCES public.clientes(id),
    cliente_nombre_anon VARCHAR(255),
    sale_date           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    notes               TEXT
);

COMMENT ON TABLE  public.sales IS 'Cabecera de ventas realizadas';
COMMENT ON COLUMN public.sales.employee_id IS 'Empleado que realizó la venta';
COMMENT ON COLUMN public.sales.cliente_id IS 'Cliente registrado (si aplica)';
COMMENT ON COLUMN public.sales.cliente_nombre_anon IS 'Nombre de cliente anónimo si no está registrado';

-- Tabla de detalles de venta
CREATE TABLE public.sales_details (
    id         SERIAL        PRIMARY KEY,
    sale_id    INTEGER       NOT NULL REFERENCES public.sales(id) ON DELETE CASCADE,
    article_id INTEGER       NOT NULL REFERENCES public.articles(id),
    amount     INTEGER       NOT NULL CHECK (amount > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price > 0),
    
    CONSTRAINT uq_sale_article UNIQUE (sale_id, article_id)
);

COMMENT ON TABLE  public.sales_details IS 'Detalles de artículos vendidos en cada venta';
COMMENT ON COLUMN public.sales_details.amount IS 'Cantidad de unidades vendidas';
COMMENT ON COLUMN public.sales_details.unit_price IS 'Precio unitario al momento de la venta';

-- ============================================================
-- 8. AUDITORÍA Y SECUENCIAS
-- ============================================================

-- Tabla de auditoría universal
CREATE TABLE public.audit_log (
    id          BIGSERIAL   PRIMARY KEY,
    table_name  VARCHAR(50) NOT NULL,
    operation   VARCHAR(10) NOT NULL,
    record_id   INTEGER,
    employee_id UUID,
    old_data    JSONB,
    new_data    JSONB,
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  public.audit_log IS 'Log de auditoría de cambios en tablas críticas';
COMMENT ON COLUMN public.audit_log.operation IS 'INSERT, UPDATE o DELETE';
COMMENT ON COLUMN public.audit_log.old_data IS 'Estado anterior del registro (UPDATE/DELETE)';
COMMENT ON COLUMN public.audit_log.new_data IS 'Estado nuevo del registro (INSERT/UPDATE)';

-- Tabla para secuencia de facturas por día
CREATE TABLE public.invoice_sequence (
    seq_date    DATE    PRIMARY KEY,
    last_number INTEGER NOT NULL DEFAULT 0
);

COMMENT ON TABLE  public.invoice_sequence IS 'Secuencia de numeración de facturas por día';
COMMENT ON COLUMN public.invoice_sequence.last_number IS 'Último número de factura emitido en seq_date';

-- ============================================================
-- 9. FUNCIONES AUXILIARES
-- ============================================================

-- 9.1 Actualización automática de timestamp
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION public.set_updated_at() IS 
'Trigger function: actualiza automáticamente el campo updated_at';

-- 9.2 Sincronización Auth → Employees
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.employees (id, email, full_name, rol)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email),
        'Empleado'
    );
    RETURN NEW;
EXCEPTION
    WHEN unique_violation THEN
        -- El empleado ya existe, esto puede suceder si se ejecuta backfill
        RETURN NEW;
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error al crear empleado: %', SQLERRM;
END;
$$;

COMMENT ON FUNCTION public.handle_new_user() IS 
'Trigger function: crea automáticamente un registro en employees cuando se crea un usuario en auth.users';

-- 9.3 Función de auditoría universal
CREATE OR REPLACE FUNCTION public.fn_audit_trigger()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public AS $$
BEGIN
    INSERT INTO public.audit_log (table_name, operation, record_id, old_data, new_data)
    VALUES (
        TG_TABLE_NAME,
        TG_OP,
        CASE 
            WHEN TG_OP = 'DELETE' THEN OLD.id 
            ELSE NEW.id 
        END,
        CASE 
            WHEN TG_OP IN ('UPDATE', 'DELETE') THEN to_jsonb(OLD) 
            ELSE NULL 
        END,
        CASE 
            WHEN TG_OP IN ('INSERT', 'UPDATE') THEN to_jsonb(NEW) 
            ELSE NULL 
        END
    );
    RETURN NULL; -- AFTER trigger, el valor de retorno no importa
END;
$$;

COMMENT ON FUNCTION public.fn_audit_trigger() IS 
'Trigger function: registra cambios en audit_log para INSERT, UPDATE y DELETE';

-- 9.4 Venta atómica con control de stock
CREATE OR REPLACE FUNCTION public.register_sale(
    p_employee_id UUID,
    p_cliente_id  INTEGER,
    p_items       JSONB,
    p_nombre_anon TEXT DEFAULT NULL
)
RETURNS INTEGER
LANGUAGE plpgsql AS $$
DECLARE
    v_sale_id INTEGER;
    v_item    JSONB;
    v_stock   INTEGER;
BEGIN
    -- Validar que la venta no esté vacía
    IF p_items IS NULL OR jsonb_array_length(p_items) = 0 THEN
        RAISE EXCEPTION 'No se puede registrar una venta vacía'
            USING ERRCODE = 'CV003';
    END IF;

    -- Crear la venta (cabecera)
    INSERT INTO public.sales (employee_id, cliente_id, cliente_nombre_anon)
    VALUES (p_employee_id, p_cliente_id, p_nombre_anon)
    RETURNING id INTO v_sale_id;

    -- Procesar cada artículo de la venta
    FOR v_item IN SELECT * FROM jsonb_array_elements(p_items) LOOP
        -- Obtener stock actual con bloqueo pesimista
        SELECT amount INTO v_stock
        FROM public.articles
        WHERE id = (v_item->>'article_id')::INTEGER
        FOR UPDATE;

        -- Validar existencia del artículo
        IF v_stock IS NULL THEN
            RAISE EXCEPTION 'Artículo ID % no existe', (v_item->>'article_id')
                USING ERRCODE = 'CV002';
        END IF;

        -- Validar stock suficiente
        IF v_stock < (v_item->>'amount')::INTEGER THEN
            RAISE EXCEPTION 'Stock insuficiente para artículo ID %. Disponible: %, Solicitado: %', 
                (v_item->>'article_id'), v_stock, (v_item->>'amount')::INTEGER
                USING ERRCODE = 'CV001';
        END IF;

        -- Insertar detalle de venta
        INSERT INTO public.sales_details (sale_id, article_id, amount, unit_price)
        VALUES (
            v_sale_id,
            (v_item->>'article_id')::INTEGER,
            (v_item->>'amount')::INTEGER,
            (v_item->>'unit_price')::NUMERIC
        );

        -- Descontar del stock
        UPDATE public.articles
        SET amount = amount - (v_item->>'amount')::INTEGER
        WHERE id = (v_item->>'article_id')::INTEGER;
    END LOOP;

    RETURN v_sale_id;
END;
$$;

COMMENT ON FUNCTION public.register_sale(UUID, INTEGER, JSONB, TEXT) IS 
'Registra una venta de forma atómica con control de stock y bloqueo pesimista.
Parámetros:
  - p_employee_id: UUID del empleado que realiza la venta
  - p_cliente_id: ID del cliente (NULL si es venta anónima)
  - p_items: Array JSON de objetos {article_id, amount, unit_price}
  - p_nombre_anon: Nombre del cliente anónimo (opcional)
Retorna: ID de la venta creada
Códigos de error:
  - CV001: Stock insuficiente
  - CV002: Artículo no existe
  - CV003: Venta vacía';

-- 9.5 Generador de número de factura secuencial por día
CREATE OR REPLACE FUNCTION public.next_invoice_number()
RETURNS TEXT
LANGUAGE plpgsql AS $$
DECLARE
    v_today DATE    := CURRENT_DATE;
    v_num   INTEGER;
BEGIN
    INSERT INTO public.invoice_sequence (seq_date, last_number)
    VALUES (v_today, 1)
    ON CONFLICT (seq_date) DO UPDATE
        SET last_number = invoice_sequence.last_number + 1
    RETURNING last_number INTO v_num;

    RETURN 'FACT-' || TO_CHAR(v_today, 'YYYYMMDD') || '-' || LPAD(v_num::TEXT, 4, '0');
END;
$$;

COMMENT ON FUNCTION public.next_invoice_number() IS 
'Genera el siguiente número de factura en formato FACT-YYYYMMDD-NNNN';

-- 9.6 Vencimiento automático de empeños
CREATE OR REPLACE FUNCTION public.fn_expire_overdue_pawns()
RETURNS INTEGER
LANGUAGE plpgsql AS $$
DECLARE
    v_count INTEGER;
BEGIN
    UPDATE public.pawns
    SET status = 'Vencido'
    WHERE status = 'Activo' 
      AND return_date < CURRENT_DATE;

    GET DIAGNOSTICS v_count = ROW_COUNT;
    
    RETURN v_count;
END;
$$;

COMMENT ON FUNCTION public.fn_expire_overdue_pawns() IS 
'Marca como Vencido todos los empeños activos cuya fecha de retorno ya pasó.
Retorna el número de empeños actualizados.
Se debe ejecutar periódicamente (ej: cron job diario)';

-- 9.7 Actualización de estado del empeño al registrar pago
CREATE OR REPLACE FUNCTION public.update_pawn_status_on_payment()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.is_missed THEN
        -- Es un registro de cuota perdida
        UPDATE public.pawns
        SET installments_missed = installments_missed + 1
        WHERE id = NEW.pawn_id;
    ELSE
        -- Es un pago real
        UPDATE public.pawns
        SET installments_paid   = installments_paid + 1,
            installments_missed = 0  -- Resetear cuotas perdidas al pagar
        WHERE id = NEW.pawn_id;
    END IF;

    -- Verificar si el empeño debe marcarse como Finalizado
    UPDATE public.pawns
    SET status = 'Finalizado'
    WHERE id = NEW.pawn_id
      AND status = 'Activo'
      AND installments_paid >= installment_count;

    RETURN NEW;
END;
$$;

COMMENT ON FUNCTION public.update_pawn_status_on_payment() IS 
'Trigger function: actualiza contadores de cuotas y estado del empeño al registrar pagos';

-- 9.8 Helper para obtener el rol del usuario actual (para políticas RLS)
CREATE OR REPLACE FUNCTION public.get_my_role()
RETURNS public.role_user
LANGUAGE sql
SECURITY DEFINER
SET search_path = public
STABLE AS $$
    SELECT rol 
    FROM public.employees 
    WHERE id = auth.uid();
$$;

COMMENT ON FUNCTION public.get_my_role() IS 
'Retorna el rol del usuario autenticado actual.
Usado internamente por políticas RLS para evitar recursión';

-- ============================================================
-- 10. TRIGGERS
-- ============================================================

-- Sincronización automática auth.users → employees
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW 
    EXECUTE FUNCTION public.handle_new_user();

-- Timestamps automáticos en actualización
CREATE TRIGGER trg_employee_upd 
    BEFORE UPDATE ON public.employees 
    FOR EACH ROW 
    EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_cliente_upd  
    BEFORE UPDATE ON public.clientes 
    FOR EACH ROW 
    EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_article_upd  
    BEFORE UPDATE ON public.articles 
    FOR EACH ROW 
    EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_pawn_upd     
    BEFORE UPDATE ON public.pawns 
    FOR EACH ROW 
    EXECUTE FUNCTION public.set_updated_at();

-- Auditoría en tablas críticas
CREATE TRIGGER trg_audit_articles 
    AFTER INSERT OR UPDATE OR DELETE ON public.articles     
    FOR EACH ROW 
    EXECUTE FUNCTION public.fn_audit_trigger();

CREATE TRIGGER trg_audit_pawns    
    AFTER INSERT OR UPDATE OR DELETE ON public.pawns        
    FOR EACH ROW 
    EXECUTE FUNCTION public.fn_audit_trigger();

CREATE TRIGGER trg_audit_sales    
    AFTER INSERT OR UPDATE OR DELETE ON public.sales        
    FOR EACH ROW 
    EXECUTE FUNCTION public.fn_audit_trigger();

CREATE TRIGGER trg_audit_purchases 
    AFTER INSERT OR UPDATE OR DELETE ON public.purchases   
    FOR EACH ROW 
    EXECUTE FUNCTION public.fn_audit_trigger();

-- Actualización automática de estado de empeño al pagar cuota
CREATE TRIGGER trg_pawn_payment_after
    AFTER INSERT ON public.pawn_payments
    FOR EACH ROW 
    EXECUTE FUNCTION public.update_pawn_status_on_payment();

-- ============================================================
-- 11. ROW LEVEL SECURITY (RLS)
-- ============================================================

-- Activar RLS en todas las tablas
ALTER TABLE public.employees     ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clientes      ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.articles      ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.purchases     ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pawns         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pawn_payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sales         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sales_details ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_log     ENABLE ROW LEVEL SECURITY;

-- ============================================================
-- 11.1 Políticas para EMPLOYEES
-- ============================================================

CREATE POLICY "employees: select authenticated"
    ON public.employees 
    FOR SELECT 
    TO authenticated 
    USING (true);

CREATE POLICY "employees: insert via trigger"
    ON public.employees 
    FOR INSERT 
    WITH CHECK (true);

CREATE POLICY "employees: update own profile"
    ON public.employees 
    FOR UPDATE 
    TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

CREATE POLICY "employees: admin update any"
    ON public.employees 
    FOR UPDATE 
    TO authenticated
    USING (public.get_my_role() = 'Admin')
    WITH CHECK (public.get_my_role() = 'Admin');

-- ============================================================
-- 11.2 Políticas para CLIENTES
-- ============================================================

CREATE POLICY "clientes: authenticated full access"
    ON public.clientes 
    FOR ALL 
    TO authenticated 
    USING (true) 
    WITH CHECK (true);

-- ============================================================
-- 11.3 Políticas para ARTICLES
-- ============================================================

CREATE POLICY "articles: select all"
    ON public.articles 
    FOR SELECT 
    TO authenticated 
    USING (true);

CREATE POLICY "articles: insert all"
    ON public.articles 
    FOR INSERT 
    TO authenticated 
    WITH CHECK (true);

CREATE POLICY "articles: update all"
    ON public.articles 
    FOR UPDATE 
    TO authenticated 
    USING (true)
    WITH CHECK (true);

CREATE POLICY "articles: delete admin only"
    ON public.articles 
    FOR DELETE 
    TO authenticated 
    USING (public.get_my_role() = 'Admin');

-- ============================================================
-- 11.4 Políticas para PURCHASES
-- ============================================================

CREATE POLICY "purchases: authenticated full access"
    ON public.purchases 
    FOR ALL 
    TO authenticated 
    USING (true) 
    WITH CHECK (true);

-- ============================================================
-- 11.5 Políticas para PAWNS
-- ============================================================

CREATE POLICY "pawns: authenticated full access"
    ON public.pawns 
    FOR ALL 
    TO authenticated 
    USING (true) 
    WITH CHECK (true);

-- ============================================================
-- 11.6 Políticas para PAWN_PAYMENTS
-- ============================================================

CREATE POLICY "pawn_payments: authenticated full access"
    ON public.pawn_payments 
    FOR ALL 
    TO authenticated 
    USING (true) 
    WITH CHECK (true);

-- ============================================================
-- 11.7 Políticas para SALES
-- ============================================================

CREATE POLICY "sales: select all"
    ON public.sales 
    FOR SELECT 
    TO authenticated 
    USING (true);

CREATE POLICY "sales: insert all"
    ON public.sales 
    FOR INSERT 
    TO authenticated 
    WITH CHECK (true);

-- Nota: No hay políticas de UPDATE/DELETE en sales por inmutabilidad de auditoría

-- ============================================================
-- 11.8 Políticas para SALES_DETAILS
-- ============================================================

CREATE POLICY "sales_details: select all"
    ON public.sales_details 
    FOR SELECT 
    TO authenticated 
    USING (true);

CREATE POLICY "sales_details: insert all"
    ON public.sales_details 
    FOR INSERT 
    TO authenticated 
    WITH CHECK (true);

-- ============================================================
-- 11.9 Políticas para AUDIT_LOG
-- ============================================================

CREATE POLICY "audit_log: admin select only"
    ON public.audit_log 
    FOR SELECT 
    TO authenticated
    USING (public.get_my_role() = 'Admin');

CREATE POLICY "audit_log: insert via trigger"
    ON public.audit_log 
    FOR INSERT 
    WITH CHECK (true);

-- ============================================================
-- 12. ÍNDICES PARA OPTIMIZACIÓN DE CONSULTAS
-- ============================================================

-- Índices para búsqueda de texto difusa (trigram)
CREATE INDEX idx_art_name_trgm   ON public.articles USING GIN (name_article gin_trgm_ops);
CREATE INDEX idx_cli_fname_trgm  ON public.clientes USING GIN (first_name  gin_trgm_ops);
CREATE INDEX idx_cli_lname_trgm  ON public.clientes USING GIN (last_name   gin_trgm_ops);
CREATE INDEX idx_cli_phone_trgm  ON public.clientes USING GIN (phone       gin_trgm_ops);
CREATE INDEX idx_cli_cedula_trgm ON public.clientes USING GIN (cedula      gin_trgm_ops);

-- Índices operacionales
CREATE INDEX idx_pawns_status_date     ON public.pawns    (status, return_date);
CREATE INDEX idx_pawns_cliente         ON public.pawns    (cliente_id);
CREATE INDEX idx_pawns_article         ON public.pawns    (article_id);
CREATE INDEX idx_pawns_employee        ON public.pawns    (employee_id);

CREATE INDEX idx_sales_date            ON public.sales    (sale_date DESC);
CREATE INDEX idx_sales_employee        ON public.sales    (employee_id);
CREATE INDEX idx_sales_cliente         ON public.sales    (cliente_id);

CREATE INDEX idx_sales_details_sale    ON public.sales_details (sale_id);
CREATE INDEX idx_sales_details_article ON public.sales_details (article_id);

CREATE INDEX idx_purchases_employee    ON public.purchases (employee_id);
CREATE INDEX idx_purchases_cliente     ON public.purchases (cliente_id);
CREATE INDEX idx_purchases_date        ON public.purchases (purchase_date DESC);

CREATE INDEX idx_pawn_payments_pawn    ON public.pawn_payments (pawn_id);
CREATE INDEX idx_pawn_payments_date    ON public.pawn_payments (payment_date DESC);

-- Índices parciales para consultas frecuentes
CREATE INDEX idx_articles_available    ON public.articles (id) WHERE amount > 0;
CREATE INDEX idx_articles_category     ON public.articles (category);
CREATE INDEX idx_pawns_active          ON public.pawns    (id) WHERE status = 'Activo';
CREATE INDEX idx_pawns_overdue         ON public.pawns    (id) WHERE status = 'Vencido';

-- Índices de auditoría
CREATE INDEX idx_audit_changed_at      ON public.audit_log (changed_at DESC);
CREATE INDEX idx_audit_table_op        ON public.audit_log (table_name, operation);
CREATE INDEX idx_audit_employee        ON public.audit_log (employee_id) WHERE employee_id IS NOT NULL;

-- ============================================================
-- 13. VISTA DE DASHBOARD
-- ============================================================

CREATE OR REPLACE VIEW public.v_dashboard AS
SELECT
    -- Empeños
    (SELECT COUNT(*) 
     FROM public.pawns 
     WHERE status = 'Activo') AS active_pawns,
    
    (SELECT COUNT(*) 
     FROM public.pawns 
     WHERE status = 'Vencido') AS overdue_pawns,
    
    COALESCE(
        (SELECT SUM(price * amount) 
         FROM public.pawns 
         WHERE status = 'Activo'), 0
    ) AS total_active_pawn_value,
    
    -- Inventario
    (SELECT COUNT(*) 
     FROM public.articles 
     WHERE amount > 0) AS total_articles_stock,
    
    COALESCE(
        (SELECT SUM(price * amount) 
         FROM public.articles 
         WHERE amount > 0), 0
    ) AS total_inventory_value,
    
    -- Clientes
    (SELECT COUNT(*) 
     FROM public.clientes 
     WHERE status = 'Activo') AS total_clientes_activos,
    
    (SELECT COUNT(*) 
     FROM public.clientes 
     WHERE registration_type = 'RAPIDO' 
       AND status = 'Activo') AS incomplete_clients,
    
    -- Compras hoy
    (SELECT COUNT(*) 
     FROM public.purchases 
     WHERE purchase_date::date = CURRENT_DATE) AS purchases_today,
    
    -- Ventas hoy
    (SELECT COUNT(*) 
     FROM public.sales 
     WHERE sale_date::date = CURRENT_DATE) AS sales_today;

COMMENT ON VIEW public.v_dashboard IS 
'Vista de resumen para el dashboard principal del sistema';

-- ============================================================
-- 14. BACKFILL — Sincronizar usuarios existentes
-- ============================================================

-- Insertar empleados para usuarios que ya existían en auth.users
INSERT INTO public.employees (id, email, full_name, rol)
SELECT
    u.id,
    u.email,
    COALESCE(u.raw_user_meta_data->>'full_name', u.email),
    'Empleado'
FROM auth.users u
LEFT JOIN public.employees e ON e.id = u.id
WHERE e.id IS NULL
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- 15. GRANTS DE PERMISOS
-- ============================================================

-- Revocar todos los permisos default
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM anon, authenticated;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM anon, authenticated;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA public FROM anon, authenticated;

-- Otorgar permisos necesarios a usuarios autenticados
GRANT USAGE ON SCHEMA public TO authenticated;

-- Permisos en tablas
GRANT SELECT, INSERT, UPDATE ON public.employees TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.clientes TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.articles TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.purchases TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.pawns TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.pawn_payments TO authenticated;
GRANT SELECT, INSERT ON public.sales TO authenticated;
GRANT SELECT, INSERT ON public.sales_details TO authenticated;
GRANT SELECT ON public.audit_log TO authenticated;
GRANT SELECT, INSERT, UPDATE ON public.invoice_sequence TO authenticated;

-- Permisos en secuencias
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO authenticated;

-- Permisos en funciones
GRANT EXECUTE ON FUNCTION public.next_invoice_number() TO authenticated;
GRANT EXECUTE ON FUNCTION public.register_sale(UUID, INTEGER, JSONB, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.fn_expire_overdue_pawns() TO authenticated;
GRANT EXECUTE ON FUNCTION public.get_my_role() TO authenticated;

-- Permisos en vistas
GRANT SELECT ON public.v_dashboard TO authenticated;

-- ============================================================
-- FIN DEL SCRIPT DDL v7.0.0
-- ============================================================

-- Verificación básica
DO $$
DECLARE
    v_tables_count INTEGER;
    v_triggers_count INTEGER;
    v_functions_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_tables_count 
    FROM information_schema.tables 
    WHERE table_schema = 'public' 
      AND table_type = 'BASE TABLE';
    
    SELECT COUNT(*) INTO v_triggers_count 
    FROM information_schema.triggers 
    WHERE trigger_schema = 'public';
    
    SELECT COUNT(*) INTO v_functions_count 
    FROM information_schema.routines 
    WHERE routine_schema = 'public' 
      AND routine_type = 'FUNCTION';
    
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'VERIFICACIÓN DE INSTALACIÓN';
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Tablas creadas:      %', v_tables_count;
    RAISE NOTICE 'Triggers creados:    %', v_triggers_count;
    RAISE NOTICE 'Funciones creadas:   %', v_functions_count;
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'DDL v7.0.0 ejecutado exitosamente';
    RAISE NOTICE '==========================================';
END $$;