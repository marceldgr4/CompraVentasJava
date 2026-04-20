package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.domain.Pawn;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operaciones CRUD sobre la tabla pawns.
 * Incluye queries con JOIN para traer datos relacionados (profile, article,
 * cliente).
 */
public class PawnDao {

    /**
     * Obtiene todos los empeños con información relacionada.
     * Incluye nombre del empleado, artículo y cliente.
     */
    public List<Pawn> findAll() throws SQLException {
        String sql = """
                SELECT
                    p.id, p.profile_id, p.article_id, p.cliente_id, p.amount,
                    p.price, p.pawn_date, p.return_date, p.expired, p.returned, p.updated_at,
                    pr.full_name AS profile_name,
                    a.name_article AS article_name,
                    CONCAT(c.first_name, ' ', c.last_name) AS cliente_name
                FROM public.pawns p
                LEFT JOIN public.profile pr ON p.profile_id = pr.id
                LEFT JOIN public.articles a ON p.article_id = a.id
                LEFT JOIN public.clientes c ON p.cliente_id = c.id
                ORDER BY p.pawn_date DESC, p.id DESC
                """;
        List<Pawn> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Busca un empeño por ID con información relacionada.
     */
    public Optional<Pawn> findById(int id) throws SQLException {
        String sql = """
                SELECT
                    p.id, p.profile_id, p.article_id, p.cliente_id, p.amount,
                    p.price, p.pawn_date, p.return_date, p.expired, p.returned, p.updated_at,
                    pr.full_name AS profile_name,
                    a.name_article AS article_name,
                    CONCAT(c.first_name, ' ', c.last_name) AS cliente_name
                FROM public.pawns p
                LEFT JOIN public.profile pr ON p.profile_id = pr.id
                LEFT JOIN public.articles a ON p.article_id = a.id
                LEFT JOIN public.clientes c ON p.cliente_id = c.id
                WHERE p.id = ?
                """;
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Obtiene empeños de un empleado específico.
     *
     * @param profileId UUID del empleado
     */
    public List<Pawn> findByProfile(String profileId) throws SQLException {
        String sql = """
                SELECT
                    p.id, p.profile_id, p.article_id, p.cliente_id, p.amount,
                    p.price, p.pawn_date, p.return_date, p.expired, p.returned, p.updated_at,
                    pr.full_name AS profile_name,
                    a.name_article AS article_name,
                    CONCAT(c.first_name, ' ', c.last_name) AS cliente_name
                FROM public.pawns p
                LEFT JOIN public.profile pr ON p.profile_id = pr.id
                LEFT JOIN public.articles a ON p.article_id = a.id
                LEFT JOIN public.clientes c ON p.cliente_id = c.id
                WHERE p.profile_id = ?::uuid
                ORDER BY p.pawn_date DESC
                """;
        List<Pawn> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Obtiene empeños activos (no devueltos ni expirados).
     */
    public List<Pawn> findActive() throws SQLException {
        String sql = """
                SELECT
                    p.id, p.profile_id, p.article_id, p.cliente_id, p.amount,
                    p.price, p.pawn_date, p.return_date, p.expired, p.returned, p.updated_at,
                    pr.full_name AS profile_name,
                    a.name_article AS article_name,
                    CONCAT(c.first_name, ' ', c.last_name) AS cliente_name
                FROM public.pawns p
                LEFT JOIN public.profile pr ON p.profile_id = pr.id
                LEFT JOIN public.articles a ON p.article_id = a.id
                LEFT JOIN public.clientes c ON p.cliente_id = c.id
                WHERE p.returned = false AND p.expired = false
                ORDER BY p.return_date ASC
                """;
        List<Pawn> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Obtiene empeños vencidos (pasaron la fecha de devolución y no fueron
     * devueltos).
     */
    public List<Pawn> findOverdue() throws SQLException {
        String sql = """
                SELECT
                    p.id, p.profile_id, p.article_id, p.cliente_id, p.amount,
                    p.price, p.pawn_date, p.return_date, p.expired, p.returned, p.updated_at,
                    pr.full_name AS profile_name,
                    a.name_article AS article_name,
                    CONCAT(c.first_name, ' ', c.last_name) AS cliente_name
                FROM public.pawns p
                LEFT JOIN public.profile pr ON p.profile_id = pr.id
                LEFT JOIN public.articles a ON p.article_id = a.id
                LEFT JOIN public.clientes c ON p.cliente_id = c.id
                WHERE p.return_date < CURRENT_DATE
                  AND p.returned = false
                  AND p.expired = false
                ORDER BY p.return_date ASC
                """;
        List<Pawn> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Guarda un nuevo empeño.
     *
     * @return El empeño con ID y timestamp asignados
     */
    public Pawn save(Pawn pawn) throws SQLException {
        String sql = """
                INSERT INTO public.pawns(
                    profile_id, article_id, cliente_id, amount, price,
                    pawn_date, return_date, expired, returned
                )
                VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id, updated_at
                """;
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pawn.getProfile_id());
            ps.setInt(2, pawn.getArticle_id());
            ps.setInt(3, pawn.getCliente_id());
            ps.setInt(4, pawn.getAmount());
            ps.setBigDecimal(5, pawn.getPrice());
            ps.setDate(6, Date.valueOf(pawn.getPawn_date()));
            ps.setDate(7, Date.valueOf(pawn.getReturn_date()));
            ps.setBoolean(8, pawn.isExpired());
            ps.setBoolean(9, pawn.isReturned());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pawn.setId(rs.getInt("id"));
                    Timestamp ts = rs.getTimestamp("updated_at");
                    if (ts != null) {
                        pawn.setUpdated_at(ts.toLocalDateTime());
                    }
                }
            }
        }
        return pawn;
    }

    /**
     * Actualiza un empeño existente.
     */
    public boolean update(Pawn pawn) throws SQLException {
        String sql = """
                UPDATE public.pawns
                SET article_id = ?,
                    cliente_id = ?,
                    amount = ?,
                    price = ?,
                    pawn_date = ?,
                    return_date = ?,
                    expired = ?,
                    returned = ?,
                    updated_at = NOW()
                WHERE id = ?
                """;
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pawn.getArticle_id());
            ps.setInt(2, pawn.getCliente_id());
            ps.setInt(3, pawn.getAmount());
            ps.setBigDecimal(4, pawn.getPrice());
            ps.setDate(5, Date.valueOf(pawn.getPawn_date()));
            ps.setDate(6, Date.valueOf(pawn.getReturn_date()));
            ps.setBoolean(7, pawn.isExpired());
            ps.setBoolean(8, pawn.isReturned());
            ps.setInt(9, pawn.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Marca un empeño como devuelto.
     *
     * @param id ID del empeño a marcar
     * @return true si se actualizó exitosamente
     */
    public boolean markAsReturned(int id) throws SQLException {
        String sql = """
                UPDATE public.pawns
                SET returned = true,
                    updated_at = NOW()
                WHERE id = ?
                """;
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Marca un empeño como expirado.
     */
    public boolean markAsExpired(int id) throws SQLException {
        String sql = """
                UPDATE public.pawns
                SET expired = true,
                    updated_at = NOW()
                WHERE id = ?
                """;
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Marca automáticamente como expirados todos los empeños vencidos.
     *
     * @return Cantidad de empeños marcados como expirados
     */
    public int expireOverduePawns() throws SQLException {
        String sql = """
                UPDATE public.pawns
                SET expired = true,
                    updated_at = NOW()
                WHERE return_date < CURRENT_DATE
                  AND returned = false
                  AND expired = false
                """;
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    /**
     * Elimina un empeño por ID.
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM public.pawns WHERE id = ?";
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Mapea un ResultSet a un objeto Pawn.
     * Incluye campos de JOIN si están disponibles.
     */
    private Pawn mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("updated_at");
        Date pawnDateSql = rs.getDate("pawn_date");
        Date returnDateSql = rs.getDate("return_date");

        Pawn pawn = new Pawn(
                rs.getInt("id"),
                rs.getString("profile_id"),
                rs.getInt("article_id"),
                rs.getInt("cliente_id"),
                rs.getInt("amount"),
                rs.getBigDecimal("price"),
                pawnDateSql != null ? pawnDateSql.toLocalDate() : null,
                returnDateSql != null ? returnDateSql.toLocalDate() : null,
                rs.getBoolean("expired"),
                rs.getBoolean("returned"),
                ts != null ? ts.toLocalDateTime() : null);

        // Campos del JOIN (pueden ser null si no se hizo JOIN)
        try {
            pawn.setProfile_name(rs.getString("profile_name"));
            pawn.setArticle_name(rs.getString("article_name"));
            pawn.setCliente_name(rs.getString("cliente_name"));
        } catch (SQLException ignored) {
            // Columnas no presentes en esta query
        }

        return pawn;
    }
}