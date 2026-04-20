package com.app.Config;

import Infrastructure.DataBase.ConnectionPool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;

public class ConexionVerificador {

    private static final String VERDE   = "\u001B[32m";
    private static final String ROJO    = "\u001B[31m";
    private static final String AMARILLO= "\u001B[33m";
    private static final String AZUL    = "\u001B[34m";
    private static final String BOLD    = "\u001B[1m";
    private static final String RESET   = "\u001B[0m";

    private static boolean jdbcOk   = false;
    private static boolean tablesOk = false;
    private static boolean authOk   = false;

    public static boolean verificarTodo() {
        print("");
        print(BOLD + "══════════════════════════════════════════════" + RESET);
        print(BOLD + "  VERIFICACIÓN DE CONEXIONES — CompraVenta    " + RESET);
        print(BOLD + "══════════════════════════════════════════════" + RESET);
        print("");

        jdbcOk   = verificarJDBC();
        tablesOk = jdbcOk && verificarTablas();
        authOk   = verificarAuth();

        print("");
        print(BOLD + "══════════════════════════════════════════════" + RESET);
        print(BOLD + "  RESUMEN" + RESET);
        print(BOLD + "══════════════════════════════════════════════" + RESET);
        estado("Conexión PostgreSQL (JDBC)",     jdbcOk);
        estado("Tablas en Supabase",             tablesOk);
        estado("Supabase Auth API",              authOk);
        print("");

        boolean allOk = jdbcOk && tablesOk && authOk;
        if (allOk) {
            print(VERDE + BOLD + "  ✔ Todo listo — iniciando aplicación." + RESET);
        } else {
            print(ROJO + BOLD + "  ✘ Hay problemas. Revisa los detalles arriba." + RESET);
        }
        print("");
        return allOk;
    }

    // ── 1. Conexión JDBC ─────────────────────────────────────
    private static boolean verificarJDBC() {
        print(AZUL + BOLD + "[ 1 ] Conexión JDBC → PostgreSQL" + RESET);
        try (Connection con = ConnectionPool.getConnection()) {
            DatabaseMetaData meta = con.getMetaData();
            print("      " + VERDE + "✔ Conectado" + RESET);
            print("      Motor  : " + meta.getDatabaseProductName()
                    + " " + meta.getDatabaseProductVersion());
            print("      Usuario: " + meta.getUserName());
            return true;
        } catch (Exception e) {
            print("      " + ROJO + "✘ Falló: " + e.getMessage() + RESET);
            print("      → Verifica DB_URL, DB_USERNAME y DB_PASSWORD en .env");
            return false;
        }
    }

    // ── 2. Tablas ─────────────────────────────────────────────
    private static boolean verificarTablas() {
        print("");
        print(AZUL + BOLD + "[ 2 ] Tablas en el esquema public" + RESET);

        String[] requeridas = {"profile", "articles", "sales", "sales_details", "pawns"};
        boolean todasOk = true;

        try (Connection con = ConnectionPool.getConnection();
             Statement st = con.createStatement()) {

            for (String tabla : requeridas) {
                ResultSet rs = st.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.tables " +
                                "WHERE table_schema='public' AND table_name='" + tabla + "'"
                );
                rs.next();
                boolean existe = rs.getInt(1) > 0;

                if (existe) {
                    ResultSet rc = st.executeQuery("SELECT COUNT(*) FROM public." + tabla);
                    rc.next();
                    int filas = rc.getInt(1);
                    print(String.format("      " + VERDE + "✔ %-20s" + RESET + "(%d filas)", tabla, filas));
                } else {
                    print(String.format("      " + ROJO + "✘ %-20s NO EXISTE" + RESET, tabla));
                    todasOk = false;
                }
            }

            // Verificar trigger
            ResultSet rt = st.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.triggers " +
                            "WHERE trigger_name='on_auth_user_created'"
            );
            rt.next();
            boolean triggerOk = rt.getInt(1) > 0;
            print("      " + (triggerOk ? VERDE+"✔" : ROJO+"✘") +
                    " trigger on_auth_user_created" + RESET);

            // Verificar RLS
            print("");
            print("      Row Level Security:");
            ResultSet rls = st.executeQuery(
                    "SELECT tablename, rowsecurity FROM pg_tables " +
                            "WHERE schemaname='public' AND tablename IN " +
                            "('profile','articles','sales','sales_details','pawns') " +
                            "ORDER BY tablename"
            );
            while (rls.next()) {
                boolean rlsOn = rls.getBoolean("rowsecurity");
                print(String.format("        %s%-22s%s RLS %s",
                        rlsOn ? VERDE+"✔" : AMARILLO+"⚠",
                        rls.getString("tablename"), RESET,
                        rlsOn ? VERDE+"activo"+RESET : AMARILLO+"INACTIVO"+RESET));
            }

            return todasOk && triggerOk;

        } catch (Exception e) {
            print("      " + ROJO + "✘ Error: " + e.getMessage() + RESET);
            return false;
        }
    }

    // ── 3. Supabase Auth API ──────────────────────────────────
    private static boolean verificarAuth() {
        print("");
        print(AZUL + BOLD + "[ 3 ] Supabase Auth REST API" + RESET);
        try {
            String url     = AppConfig.get("SUPABASE_URL");
            String anonKey = AppConfig.get("SUPABASE_ANON_KEY");

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/auth/v1/settings"))
                    .header("apikey", anonKey)
                    .timeout(Duration.ofSeconds(10))
                    .GET().build();

            HttpResponse<String> res =
                    HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 200) {
                JsonNode json = new ObjectMapper().readTree(res.body());
                print("      " + VERDE + "✔ Conectado a Supabase Auth" + RESET);
                print("      URL     : " + url);
                print("      AnonKey : " + anonKey.substring(0, 20) + "...");
                if (json.has("disable_signup")) {
                    boolean off = json.get("disable_signup").asBoolean();
                    print("      Signup  : " + (off
                            ? AMARILLO + "deshabilitado" + RESET
                            : VERDE    + "habilitado"   + RESET));
                }
                return true;
            } else {
                print("      " + ROJO + "✘ HTTP " + res.statusCode() + RESET);
                return false;
            }
        } catch (Exception e) {
            print("      " + ROJO + "✘ " + e.getMessage() + RESET);
            print("      → Verifica SUPABASE_URL y SUPABASE_ANON_KEY en .env");
            return false;
        }
    }

    private static void estado(String nombre, boolean ok) {
        System.out.printf("  %s%-42s%s %s%n",
                ok ? VERDE : ROJO,
                (ok ? "✔ " : "✘ ") + nombre,
                RESET,
                ok ? VERDE+"OK"+RESET : ROJO+"FALLO"+RESET);
    }

    private static void print(String s) { System.out.println(s); }
}