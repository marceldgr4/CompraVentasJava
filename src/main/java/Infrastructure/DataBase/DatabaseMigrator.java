package Infrastructure.DataBase;

import Infrastructure.logging.LoggerFactory;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigrator {
    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrator.class);

    public static void verifyAndMigrate(Connection con) {
        log.info("Iniciando verificación y migración automática de esquema de base de datos (v7)...");
        try (Statement st = con.createStatement()) {

            // 1. Verificar y renombrar en purchases
            if (hasColumn(con, "purchases", "profile_id")) {
                log.info("Migrando tabla purchases: renombrando profile_id a employee_id...");
                st.executeUpdate("ALTER TABLE public.purchases RENAME COLUMN profile_id TO employee_id");
            }

            // 2. Verificar y renombrar en sales
            if (hasColumn(con, "sales", "profile_id")) {
                log.info("Migrando tabla sales: renombrando profile_id a employee_id...");
                st.executeUpdate("ALTER TABLE public.sales RENAME COLUMN profile_id TO employee_id");
            }

            // 3. Verificar y renombrar en pawns
            if (hasColumn(con, "pawns", "profile_id")) {
                log.info("Migrando tabla pawns: renombrando profile_id a employee_id...");
                st.executeUpdate("ALTER TABLE public.pawns RENAME COLUMN profile_id TO employee_id");
            }

            // 4. Verificar y renombrar en pawn_payments
            if (hasColumn(con, "pawn_payments", "created_by_profile_id")) {
                log.info("Migrando tabla pawn_payments: renombrando created_by_profile_id a created_by_employee_id...");
                st.executeUpdate("ALTER TABLE public.pawn_payments RENAME COLUMN created_by_profile_id TO created_by_employee_id");
            }

            // 5. Verificar y renombrar en audit_log
            if (hasColumn(con, "audit_log", "profile_id")) {
                log.info("Migrando tabla audit_log: renombrando profile_id a employee_id...");
                st.executeUpdate("ALTER TABLE public.audit_log RENAME COLUMN profile_id TO employee_id");
            }

            // 6. Actualizar la stored procedure register_sale para usar employee_id
            log.info("Verificando/Actualizando stored procedure register_sale...");
            st.execute("DROP FUNCTION IF EXISTS public.register_sale(uuid, integer, jsonb, text);");
            String spSql = """
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
                """;
            st.execute(spSql);

            log.info("Migración y verificación de esquema completada exitosamente.");
        } catch (Exception e) {
            log.error("Error durante la migración automática de la base de datos", e);
        }
    }

    private static boolean hasColumn(Connection con, String tableName, String columnName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = ? AND column_name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
