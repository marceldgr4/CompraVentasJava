package com.app.Infrastructure.DataBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;


public final class DataBaseManeger {

    private static final Logger log = LoggerFactory.getLogger(DataBaseManeger.class);

    private DataBaseManeger() {
        // Clase utilitaria: no instanciar
    }

    /**
     * Obtiene una conexión del pool.
     *
     * @return conexión activa
     * @throws SQLException si no hay conexiones disponibles
     */
    public static Connection getConnection() throws SQLException {
        return ConnectionPool.getConnection();
    }

    /**
     * Ejecuta un bloque de trabajo dentro de una transacción.
     * Si el bloque lanza una excepción, se hace rollback.
     * Si termina normalmente, se hace commit.
     *
     * @param work bloque de operaciones a ejecutar en la transacción
     * @throws SQLException si falla al obtener conexión, en el commit o el rollback
     */
    public static void runInTransaction(TransactionalWork work) throws SQLException {
        Connection con = null;
        try {
            con = ConnectionPool.getConnection();
            con.setAutoCommit(false);
            work.execute(con);
            con.commit();
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException rb) {
                    log.error("Error al hacer rollback", rb);
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException cl) {
                    log.warn("Error al cerrar conexión transaccional", cl);
                }
            }
        }
    }

    /**
     * Interfaz funcional para el bloque de trabajo transaccional.
     */
    @FunctionalInterface
    public interface TransactionalWork {
        /**
         * Ejecuta las operaciones de base de datos dentro de la transacción.
         *
         * @param con conexión activa
         * @throws SQLException si alguna operación falla
         */
        void execute(Connection con) throws SQLException;
    }
}
