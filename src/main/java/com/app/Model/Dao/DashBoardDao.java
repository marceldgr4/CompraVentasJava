package com.app.Model.Dao;

import com.app.Infrastructure.DataBase.ConnectionPool;
import com.app.Model.domain.DashBoardDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashBoardDao {

    public DashBoardDto getDashboardMetric() throws SQLException {
        try (Connection con = ConnectionPool.getConnection()) {
            return queryView(con);
        }
    }

    // Lee desde v_dashboard — columnas alineadas con database_v6.sql
    private DashBoardDto queryView(Connection con) throws SQLException {
        String sql = "SELECT * FROM public.v_dashboard";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new DashBoardDto(
                        rs.getInt("active_pawns"),
                        rs.getInt("overdue_pawns"),
                        rs.getInt("total_articles_stock"),    // nombre correcto en v6
                        rs.getInt("total_clientes_activos"),  // nombre correcto en v6
                        rs.getBigDecimal("total_active_pawn_value"),
                        rs.getInt("incomplete_clients"),
                        rs.getInt("purchases_today")
                );
            }
        } catch (SQLException e) {
            // La vista no existe o las columnas cambiaron: usar fallback
            return queryFallback(con);
        }
        return new DashBoardDto();
    }

    // Fallback con CTEs directas cuando la vista no está disponible
    private DashBoardDto queryFallback(Connection con) throws SQLException {
        String sql = """
                WITH
                active_pawns AS (
                    SELECT COUNT(*) AS cnt,
                           COALESCE(SUM(price * amount), 0) AS total_value
                    FROM public.pawns
                    WHERE status = 'Activo'
                ),
                overdue_pawns AS (
                    SELECT COUNT(*) AS cnt
                    FROM public.pawns
                    WHERE status = 'Vencido'
                ),
                articles_stock AS (
                    SELECT COUNT(*) AS cnt
                    FROM public.articles
                    WHERE amount > 0
                ),
                active_clients AS (
                    SELECT COUNT(*) AS cnt
                    FROM public.clientes
                    WHERE status = 'Activo'
                ),
                incomplete AS (
                    SELECT COUNT(*) AS cnt
                    FROM public.clientes
                    WHERE registration_type = 'RAPIDO'
                      AND status = 'Activo'
                )
                SELECT
                    (SELECT cnt FROM active_pawns)   AS active_pawns,
                    (SELECT cnt FROM overdue_pawns)  AS overdue_pawns,
                    (SELECT cnt FROM articles_stock) AS total_articles_stock,
                    (SELECT cnt FROM active_clients) AS total_clientes_activos,
                    (SELECT total_value FROM active_pawns) AS total_active_pawn_value,
                    (SELECT cnt FROM incomplete)     AS incomplete_clients,
                    (SELECT COUNT(*) FROM public.purchases WHERE purchase_date::date = CURRENT_DATE) AS purchases_today
                """;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new DashBoardDto(
                        rs.getInt("active_pawns"),
                        rs.getInt("overdue_pawns"),
                        rs.getInt("total_articles_stock"),
                        rs.getInt("total_clientes_activos"),
                        rs.getBigDecimal("total_active_pawn_value"),
                        rs.getInt("incomplete_clients"),
                        rs.getInt("purchases_today")
                );
            }
        }

        return new DashBoardDto();
    }
}