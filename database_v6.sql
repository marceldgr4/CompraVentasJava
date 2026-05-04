-- ============================================================
-- DDL COMPRAVENTA v6.1 — CONSOLIDADO Y CORREGIDO
-- Compatible: PostgreSQL 16 / Supabase
-- ============================================================

-- ============================================================
-- 0. LIMPIEZA TOTAL (Idempotencia)
-- ============================================================
DROP TRIGGER IF EXISTS on_auth_user_created       ON auth.users;
DROP TRIGGER IF EXISTS trg_profile_upd            ON public.profile;
DROP TRIGGER IF EXISTS trg_cliente_upd            ON public.clientes;
DROP TRIGGER IF EXISTS trg_article_upd            ON public.articles;
DROP TRIGGER IF EXISTS trg_pawn_upd               ON public.pawns;
DROP TRIGGER IF EXISTS trg_audit_articles         ON public.articles;
DROP TRIGGER IF EXISTS trg_audit_pawns            ON public.pawns;
DROP TRIGGER IF EXISTS trg_audit_sales            ON public.articles; -- Corrected from articles to sales if it existed there
DROP TRIGGER IF EXISTS trg_audit_sales            ON public.sales;
DROP TRIGGER IF EXISTS trg_pawn_payment_after     ON public.pawn_payments;

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

DROP TABLE IF EXISTS public.invoice_sequence  CASCADE;
DROP TABLE IF EXISTS public.audit_log         CASCADE;
DROP TABLE IF EXISTS public.sales_details     CASCADE;
DROP TABLE IF EXISTS public.sales             CASCADE;
DROP TABLE IF EXISTS public.purchases         CASCADE;
DROP TABLE IF EXISTS public.pawn_payments     CASCADE;
DROP TABLE IF EXISTS public.pawns             CASCADE;
DROP TABLE IF EXISTS public.articles          CASCADE;
DROP TABLE IF EXISTS public.profile           CASCADE;
DROP TABLE IF EXISTS public.clientes          CASCADE;

DROP TYPE IF EXISTS public.role_user         CASCADE;
DROP TYPE IF EXISTS public.article_category  CASCADE;
DROP TYPE IF EXISTS public.cliente_status    CASCADE;
DROP TYPE IF EXISTS public.pawn_status       CASCADE;
DROP TYPE IF EXISTS public.registration_type CASCADE;
DROP TYPE IF EXISTS public.source_type       CASCADE;
DROP TYPE IF EXISTS public.item_state        CASCADE;

-- ============================================================
-- 1. EXTENSIONES
-- ============================================================
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- 2. TIPOS ENUMERADOS
-- ============================================================
CREATE TYPE public.role_user         AS ENUM ('Admin', 'Empleado');
CREATE TYPE public.article_category  AS ENUM ('Electrodomesticos', 'Joyeria', 'Herramientas', 'Tecnologia', 'Otro');
CREATE TYPE public.cliente_status    AS ENUM ('Activo', 'Eliminado');
CREATE TYPE public.pawn_status       AS ENUM ('Activo', 'Vencido', 'Finalizado', 'Perdido', 'Retirado', 'Vendido');
CREATE TYPE public.registration_type AS ENUM ('RAPIDO', 'COMPLETO');
CREATE TYPE public.source_type       AS ENUM ('EMPENO', 'COMPRA', 'AJUSTE', 'OTRO');
CREATE TYPE public.item_state        AS ENUM ('Nuevo', 'Bueno', 'Regular', 'Dañado');

-- ============================================================
-- 3. TABLAS MAESTRAS
-- ============================================================

CREATE TABLE public.profile (
    id         UUID                 PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email      VARCHAR(255)         NOT NULL UNIQUE,
    full_name  VARCHAR(255)         NOT NULL,
    rol        public.role_user     NOT NULL DEFAULT 'Empleado',
    active     BOOLEAN              NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ          NOT NULL DEFAULT NOW()
);

CREATE TABLE public.clientes (
    id                SERIAL                      PRIMARY KEY,
    first_name        VARCHAR(255)                NOT NULL,
    last_name         VARCHAR(255),
    cedula            VARCHAR(50)                 UNIQUE CONSTRAINT chk_cedula_numeric CHECK (cedula ~ '^[0-9]+$'),
    email             VARCHAR(255)                UNIQUE,
    phone             VARCHAR(50)                 CONSTRAINT chk_phone_format CHECK (phone ~ '^[+]?[0-9]{7,15}$'),
    address           TEXT,
    city              VARCHAR(100),
    registration_type public.registration_type    NOT NULL DEFAULT 'COMPLETO',
    status            public.cliente_status       NOT NULL DEFAULT 'Activo',
    created_at        TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ                 NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 4. INVENTARIO
-- ============================================================
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

-- ============================================================
-- 5. COMPRAS
-- ============================================================
CREATE TABLE public.purchases (
    id             SERIAL        PRIMARY KEY,
    profile_id     UUID          NOT NULL REFERENCES public.profile(id),
    cliente_id     INTEGER       NOT NULL REFERENCES public.clientes(id),
    article_id     INTEGER       NOT NULL REFERENCES public.articles(id),
    purchase_price NUMERIC(12,2) NOT NULL CHECK (purchase_price > 0),
    purchase_date  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    notes          TEXT
);

-- ============================================================
-- 6. EMPEÑOS
-- ============================================================
CREATE TABLE public.pawns (
    id                  SERIAL             PRIMARY KEY,
    profile_id          UUID               NOT NULL REFERENCES public.profile(id),
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

CREATE TABLE public.pawn_payments (
    id                    SERIAL        PRIMARY KEY,
    pawn_id               INTEGER       NOT NULL REFERENCES public.pawns(id) ON DELETE CASCADE,
    amount                NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    payment_date          DATE          NOT NULL DEFAULT CURRENT_DATE,
    notes                 VARCHAR(500),
    created_by_profile_id UUID          NOT NULL REFERENCES public.profile(id),
    is_missed             BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- ============================================================
-- 7. VENTAS
-- ============================================================
CREATE TABLE public.sales (
    id                  SERIAL      PRIMARY KEY,
    profile_id          UUID        NOT NULL REFERENCES public.profile(id),
    cliente_id          INTEGER     REFERENCES public.clientes(id),
    cliente_nombre_anon VARCHAR(255),
    sale_date           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    notes               TEXT
);

CREATE TABLE public.sales_details (
    id         SERIAL        PRIMARY KEY,
    sale_id    INTEGER       NOT NULL REFERENCES public.sales(id) ON DELETE CASCADE,
    article_id INTEGER       NOT NULL REFERENCES public.articles(id),
    amount     INTEGER       NOT NULL CHECK (amount > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price > 0),
    CONSTRAINT uq_sale_article UNIQUE (sale_id, article_id)
);

-- ============================================================
-- 8. AUDITORÍA Y SECUENCIAS
-- ============================================================
CREATE TABLE public.audit_log (
    id         BIGSERIAL   PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    operation  VARCHAR(10) NOT NULL,
    record_id  INTEGER,
    profile_id UUID,
    old_data   JSONB,
    new_data   JSONB,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE public.invoice_sequence (
    seq_date    DATE    PRIMARY KEY,
    last_number INTEGER NOT NULL DEFAULT 0
);

-- ============================================================
-- 9. FUNCIONES
-- ============================================================

-- 9.1 Timestamp automático
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$;

-- 9.2 Sincronización Auth → Profile
-- SECURITY DEFINER es obligatorio: el trigger corre como el owner
-- de la función, no como anon/authenticated, para poder insertar en profile
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.profile (id, email, full_name, rol)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.email),
        'Empleado'
    );
    RETURN NEW;
END;
$$;

-- 9.3 Auditoría universal
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
        CASE WHEN TG_OP = 'DELETE' THEN OLD.id ELSE NEW.id END,
        CASE WHEN TG_OP IN ('UPDATE', 'DELETE') THEN to_jsonb(OLD) ELSE NULL END,
        CASE WHEN TG_OP IN ('INSERT', 'UPDATE') THEN to_jsonb(NEW) ELSE NULL END
    );
    RETURN NULL;
END;
$$;

-- 9.4 Venta atómica con bloqueo pesimista
CREATE OR REPLACE FUNCTION public.register_sale(
    p_profile_id  UUID,
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
    IF jsonb_array_length(p_items) = 0 THEN
        RAISE EXCEPTION 'Venta vacía no permitida.';
    END IF;

    INSERT INTO public.sales (profile_id, cliente_id, cliente_nombre_anon)
    VALUES (p_profile_id, p_cliente_id, p_nombre_anon)
    RETURNING id INTO v_sale_id;

    FOR v_item IN SELECT * FROM jsonb_array_elements(p_items) LOOP
        SELECT amount INTO v_stock
        FROM public.articles
        WHERE id = (v_item->>'article_id')::INTEGER
        FOR UPDATE;

        IF v_stock IS NULL THEN
            RAISE EXCEPTION 'Artículo ID % no existe.', (v_item->>'article_id')
                USING ERRCODE = 'CV002';
        END IF;

        IF v_stock < (v_item->>'amount')::INTEGER THEN
            RAISE EXCEPTION 'Stock insuficiente para artículo ID %.', (v_item->>'article_id')
                USING ERRCODE = 'CV001';
        END IF;

        INSERT INTO public.sales_details (sale_id, article_id, amount, unit_price)
        VALUES (
            v_sale_id,
            (v_item->>'article_id')::INTEGER,
            (v_item->>'amount')::INTEGER,
            (v_item->>'unit_price')::NUMERIC
        );

        UPDATE public.articles
        SET amount = amount - (v_item->>'amount')::INTEGER
        WHERE id = (v_item->>'article_id')::INTEGER;
    END LOOP;

    RETURN v_sale_id;
END;
$$;

-- 9.5 Secuencia de factura segura
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

-- 9.6 Vencimiento automático de empeños
CREATE OR REPLACE FUNCTION public.fn_expire_overdue_pawns()
RETURNS INTEGER
LANGUAGE plpgsql AS $$
DECLARE
    v_count INTEGER;
BEGIN
    UPDATE public.pawns
    SET status = 'Vencido'
    WHERE status = 'Activo' AND return_date < CURRENT_DATE;

    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$;

-- 9.7 Actualización de estado por pago de cuota
CREATE OR REPLACE FUNCTION public.update_pawn_status_on_payment()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.is_missed THEN
        UPDATE public.pawns
        SET installments_missed = installments_missed + 1
        WHERE id = NEW.pawn_id;
    ELSE
        UPDATE public.pawns
        SET installments_paid   = installments_paid + 1,
            installments_missed = 0
        WHERE id = NEW.pawn_id;
    END IF;

    -- Finalizar si todas las cuotas están pagadas
    UPDATE public.pawns
    SET status = 'Finalizado'
    WHERE id = NEW.pawn_id
      AND status = 'Activo'
      AND installments_paid >= installment_count;

    RETURN NEW;
END;
$$;

-- ============================================================
-- 10. TRIGGERS
-- ============================================================

-- Auth: crear perfil automáticamente al registrar usuario
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Timestamps automáticos
CREATE TRIGGER trg_profile_upd  BEFORE UPDATE ON public.profile  FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_cliente_upd  BEFORE UPDATE ON public.clientes FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_article_upd  BEFORE UPDATE ON public.articles FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_pawn_upd     BEFORE UPDATE ON public.pawns    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- Auditoría
CREATE TRIGGER trg_audit_articles AFTER INSERT OR UPDATE OR DELETE ON public.articles     FOR EACH ROW EXECUTE FUNCTION public.fn_audit_trigger();
CREATE TRIGGER trg_audit_pawns    AFTER INSERT OR UPDATE OR DELETE ON public.pawns        FOR EACH ROW EXECUTE FUNCTION public.fn_audit_trigger();
CREATE TRIGGER trg_audit_sales    AFTER INSERT OR UPDATE OR DELETE ON public.sales        FOR EACH ROW EXECUTE FUNCTION public.fn_audit_trigger();
CREATE TRIGGER trg_audit_purchases AFTER INSERT OR UPDATE OR DELETE ON public.purchases   FOR EACH ROW EXECUTE FUNCTION public.fn_audit_trigger();

-- Pagos de empeño
CREATE TRIGGER trg_pawn_payment_after
    AFTER INSERT ON public.pawn_payments
    FOR EACH ROW EXECUTE FUNCTION public.update_pawn_status_on_payment();

-- ============================================================
-- 11. ROW LEVEL SECURITY
-- ============================================================

ALTER TABLE public.profile       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clientes      ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.articles      ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.purchases     ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pawns         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pawn_payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sales         ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sales_details ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_log     ENABLE ROW LEVEL SECURITY;

-- Helper interno para evitar recursión en políticas
CREATE OR REPLACE FUNCTION public.get_my_role()
RETURNS public.role_user
LANGUAGE sql
SECURITY DEFINER
SET search_path = public
STABLE AS $$
    SELECT rol FROM public.profile WHERE id = auth.uid();
$$;

-- profile
CREATE POLICY "profile: select authenticated"
    ON public.profile FOR SELECT TO authenticated USING (true);

CREATE POLICY "profile: insert via trigger"
    ON public.profile FOR INSERT WITH CHECK (true);

CREATE POLICY "profile: update own"
    ON public.profile FOR UPDATE TO authenticated
    USING (auth.uid() = id);

CREATE POLICY "profile: admin update any"
    ON public.profile FOR UPDATE TO authenticated
    USING (public.get_my_role() = 'Admin');

-- clientes
CREATE POLICY "clientes: authenticated all"
    ON public.clientes FOR ALL TO authenticated USING (true) WITH CHECK (true);

-- articles
CREATE POLICY "articles: select all"       ON public.articles FOR SELECT    TO authenticated USING (true);
CREATE POLICY "articles: insert all"       ON public.articles FOR INSERT    TO authenticated WITH CHECK (true);
CREATE POLICY "articles: update all"       ON public.articles FOR UPDATE    TO authenticated USING (true);
CREATE POLICY "articles: delete admin"     ON public.articles FOR DELETE    TO authenticated USING (public.get_my_role() = 'Admin');

-- purchases
CREATE POLICY "purchases: authenticated all"
    ON public.purchases FOR ALL TO authenticated USING (true) WITH CHECK (true);

-- pawns
CREATE POLICY "pawns: authenticated all"
    ON public.pawns FOR ALL TO authenticated USING (true) WITH CHECK (true);

-- pawn_payments
CREATE POLICY "pawn_payments: authenticated all"
    ON public.pawn_payments FOR ALL TO authenticated USING (true) WITH CHECK (true);

-- sales
CREATE POLICY "sales: select all"    ON public.sales FOR SELECT TO authenticated USING (true);
CREATE POLICY "sales: insert all"    ON public.sales FOR INSERT TO authenticated WITH CHECK (true);
-- Sin DELETE: ventas son inmutables por auditoría

-- sales_details
CREATE POLICY "sales_details: select all"  ON public.sales_details FOR SELECT TO authenticated USING (true);
CREATE POLICY "sales_details: insert all"  ON public.sales_details FOR INSERT TO authenticated WITH CHECK (true);

-- audit_log
CREATE POLICY "audit_log: admin select"
    ON public.audit_log FOR SELECT TO authenticated
    USING (public.get_my_role() = 'Admin');

CREATE POLICY "audit_log: insert trigger"
    ON public.audit_log FOR INSERT WITH CHECK (true);

-- ============================================================
-- 12. ÍNDICES
-- ============================================================

-- Búsqueda por texto parcial (GIN trgm — un índice por columna)
CREATE INDEX idx_art_name_trgm   ON public.articles USING GIN (name_article gin_trgm_ops);
CREATE INDEX idx_cli_fname_trgm  ON public.clientes USING GIN (first_name  gin_trgm_ops);
CREATE INDEX idx_cli_lname_trgm  ON public.clientes USING GIN (last_name   gin_trgm_ops);
CREATE INDEX idx_cli_phone_trgm  ON public.clientes USING GIN (phone       gin_trgm_ops);
CREATE INDEX idx_cli_cedula_trgm ON public.clientes USING GIN (cedula      gin_trgm_ops);

-- Operacionales
CREATE INDEX idx_pawns_status_date     ON public.pawns    (status, return_date);
CREATE INDEX idx_pawns_cliente         ON public.pawns    (cliente_id);
CREATE INDEX idx_pawns_article         ON public.pawns    (article_id);
CREATE INDEX idx_sales_date            ON public.sales    (sale_date DESC);
CREATE INDEX idx_sales_profile         ON public.sales    (profile_id);
CREATE INDEX idx_sales_cliente         ON public.sales    (cliente_id);
CREATE INDEX idx_sales_details_sale    ON public.sales_details (sale_id);
CREATE INDEX idx_sales_details_article ON public.sales_details (article_id);
CREATE INDEX idx_purchases_profile     ON public.purchases (profile_id);
CREATE INDEX idx_purchases_cliente     ON public.purchases (cliente_id);
CREATE INDEX idx_pawn_payments_pawn    ON public.pawn_payments (pawn_id);
CREATE INDEX idx_articles_available    ON public.articles (id) WHERE amount > 0;
CREATE INDEX idx_articles_category     ON public.articles (category);
CREATE INDEX idx_audit_changed_at      ON public.audit_log (changed_at DESC);
CREATE INDEX idx_audit_table_op        ON public.audit_log (table_name, operation);

-- ============================================================
-- 13. VISTA DE DASHBOARD
-- ============================================================
CREATE OR REPLACE VIEW public.v_dashboard AS
SELECT
    (SELECT COUNT(*) FROM public.pawns    WHERE status = 'Activo')              AS active_pawns,
    (SELECT COUNT(*) FROM public.pawns    WHERE status = 'Vencido')             AS overdue_pawns,
    (SELECT COUNT(*) FROM public.articles WHERE amount > 0)                     AS total_articles_stock,
    (SELECT COUNT(*) FROM public.clientes WHERE status = 'Activo')              AS total_clientes_activos,
    (SELECT COUNT(*) FROM public.clientes WHERE registration_type = 'RAPIDO')   AS incomplete_profiles,
    (SELECT COUNT(*) FROM public.purchases WHERE purchase_date::date = CURRENT_DATE) AS purchases_today,
    COALESCE(
        (SELECT SUM(price * amount) FROM public.pawns WHERE status = 'Activo'), 0
    )                                                                            AS total_active_pawn_value;

-- ============================================================
-- 14. BACKFILL — Perfiles de usuarios ya existentes en auth
--     Ejecutar solo si había usuarios antes del trigger
-- ============================================================
INSERT INTO public.profile (id, email, full_name, rol)
SELECT
    u.id,
    u.email,
    COALESCE(u.raw_user_meta_data->>'full_name', u.email),
    'Empleado'
FROM auth.users u
LEFT JOIN public.profile p ON p.id = u.id
WHERE p.id IS NULL;