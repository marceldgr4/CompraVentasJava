package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
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

    private DashBoardDto queryView(Connection con) throws SQLException {
        String sql = " SELECT * FROM public.v_dashboard";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new DashBoardDto(
                        rs.getInt("active_pawns"),
                        rs.getInt("overdue_pawns"),
                        rs.getInt("total_article"),
                        rs.getInt("total_clientes"),
                        rs.getBigDecimal("total_active_value")
                );
            }
        } catch (SQLException viewNotFound) {
            return queryFallback(con);
        }
        return new DashBoardDto();
    }

    private DashBoardDto queryFallback(Connection con) throws SQLException {
        String sql = """
                WITH
                active_pawns AS (
                    SELECT COUNT(*) AS cnt, COALESCE(SUM(price * amount), 0) AS total
                    FROM public.pawns
                    WHERE status = 'Activo'
                ),
                overdue_pawns AS (
                    SELECT COUNT(*) AS cnt
                    FROM public.pawns
                    WHERE status = 'Vencido'
                ),
                articles AS (
                    SELECT COUNT(*) AS cnt
                    FROM public.articles
                ),
                clients AS (
                    SELECT COUNT(*) AS cnt
                    FROM public.clientes
                    WHERE status = 'Activo'
                )
                SELECT
                   (SELECT cnt FROM active_pawns) AS active_pawns,
                   (SELECT cnt FROM overdue_pawns) AS overdue_pawns,
                   (SELECT cnt FROM articles) AS total_articles,
                   (SELECT cnt FROM clients) AS total_clients,
                   (SELECT total FROM active_pawns) AS total_active_value
                """;
        try (PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new DashBoardDto(
                        rs.getInt("active_pawns"),
                        rs.getInt("overdue_pawns"),
                        rs.getInt("total_articles"),
                        rs.getInt("total_clients"),
    rs.getBigDecimal("total_active_value"));
            }
        }
        return new DashBoardDto();
    }
}
