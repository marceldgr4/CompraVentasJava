package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.domain.Pawn;
import com.app.Model.Enum.PawnStatus;

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

    private static final String SELECT_COLS = """
            p.id, p.profile_id, p.article_id, p.cliente_id, p.amount,
            p.price, p.weight_grams, p.installment_count, p.installments_paid,
            p.installments_missed, p.pawn_date, p.return_date,
            p.status, p.notes, p.updated_at,
            pr.full_name  AS profile_name,
            a.name_article AS article_name,
            CONCAT(c.first_name, ' ', c.last_name) AS cliente_name
            """;

    private static final String FROM_JOINS = """
            FROM public.pawns p
            LEFT JOIN public.profile  pr ON p.profile_id  = pr.id
            LEFT JOIN public.articles  a ON p.article_id  = a.id
            LEFT JOIN public.clientes  c ON p.cliente_id  = c.id
            """;

    //----READ-----

    public List<Pawn> findAll() throws SQLException {
        String sql = "SELECT" + SELECT_COLS + FROM_JOINS + " ORDER BY p.pawn_date DESC ,p.id DESC";

        return executeList(sql, ps -> {
        });
    }

    public Optional<Pawn> findById(int id) throws SQLException {
        String sql = "SELECT" + SELECT_COLS + FROM_JOINS + " WHERE p.id = ?";
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


    public List<Pawn> findByProfile(String profileId) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + FROM_JOINS +
                "WHERE p.profile_id = ?::uuid " +
                "ORDER BY p.pawn_date DESC ";

                return executeList(sql, ps-> ps.setString(1, profileId));
    }
    public List<Pawn> findByStatus(PawnStatus status) throws SQLException {
        String sql = "SELECT" + SELECT_COLS+ FROM_JOINS+
                "WHERE p.status = ?::pawn_status "
                + "ORDER BY p.pawn_date DESC ";
        return executeList(sql, ps-> ps.setString(1,status.name()));
    }

    public List<Pawn> findActive() throws SQLException {
        return findByStatus(PawnStatus.Activo);
    }

    public List<Pawn> findOverdue() throws SQLException {
        return findByStatus(PawnStatus.Vencido);
    }
    // ── CREATE ─────────────────────────────────────────────────────────────────

    /*
     * Guarda un nuevo empeño.
     * La reducción de stock del artículo debe hacerse en la misma transacción JDBC
     * desde {@code PawnService
     */

    public Pawn save( Pawn pawn) throws SQLException {
        String sql = """
                INSERT INTO public.pawns(
                    profile_id, article_id, cliente_id, amount, price, weight_grams, installment_count,
                    installments_paid,installments_missed, pawn_date, return_date, status, notes)
                VALUES (?::uuid, ?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?::pawn_status, ?)
                RETURNING id, updated_at
                """;
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pawn.getProfileId());
            ps.setInt(2, pawn.getArticleId());
            ps.setInt(3, pawn.getClientId());
            ps.setInt(4, pawn.getAmount());
            ps.setBigDecimal(5, pawn.getPrice());

            ps.setBigDecimal(6, pawn.getWeightGrams());
            ps.setInt(7, pawn.getInstallmentCount());

            ps.setDate(8, Date.valueOf(pawn.getPawnDate()));
            ps.setDate(9, Date.valueOf(pawn.getReturnDate()));
            ps.setString(10, PawnStatus.Activo.name());
            ps.setString(11, pawn.getNotes());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pawn.setId(rs.getInt("id"));
                    Timestamp ts = rs.getTimestamp("updated_at");
                    if (ts != null) {
                        pawn.setUpdatedAt(ts.toLocalDateTime());
                    }
                }
            }
        }
        return pawn;
    }

    /*
     * Versión transaccional de {@link #save} — usa conexión externa para
     * participar en la transacción del llamador (necesario para reducir stock
     * atómicamente en el mismo commit).
     */
    public Pawn save(Connection con, Pawn pawn) throws SQLException {
        String sql = """
                INSERT INTO public.pawns(
                profile_id, article_id, cliente_id, amount, price, weight_grams, installment_count,
                installments_paid,installments_missed, pawn_date, return_date, status, notes)
                VALUES(?::UUID, ?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?::pawn_status, ?)
                RETURNING id, updated_at
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pawn.getProfileId());
            ps.setInt(2, pawn.getArticleId());
            ps.setInt(3, pawn.getClientId());
            ps.setInt(4, pawn.getAmount());
            ps.setBigDecimal(5, pawn.getPrice());
            ps.setBigDecimal(6, pawn.getWeightGrams());
            ps.setInt(7, pawn.getInstallmentCount());
            ps.setDate(8, Date.valueOf(pawn.getPawnDate()));
            ps.setDate(9, Date.valueOf(pawn.getReturnDate()));
            ps.setString(10, PawnStatus.Activo.name());
            ps.setString(11, pawn.getNotes());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pawn.setId(rs.getInt("id"));
                    Timestamp ts = rs.getTimestamp("updated_at");
                    if (ts != null) pawn.setUpdatedAt(ts.toLocalDateTime());
                }
            }
        }
        return pawn;
    }


    /**
     * Actualiza un empeño existente.
     */
    public boolean updateStatus(int id, PawnStatus newStatus) throws SQLException {
        String sql = """
                UPDATE public.pawns
                SET status = ?::pawn_status
                WHERE id = ?
                """;
        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }
    public boolean markAsReturned(int id) throws SQLException{
        return updateStatus(id, PawnStatus.Retirado);
    }
    public boolean markAsExpired(int id) throws SQLException {
        return updateStatus(id, PawnStatus.Vencido);
    }


    public int expireOverduePawns() throws SQLException {
        String sql = "SELECT  public.fn_expire_overdue_pawns()";

        try (Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()){
           if(rs.next()) return rs.getInt(1);
        }
        return 0;
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

    private List<Pawn> executeList(String sql, SqlSetter setter) throws SQLException {
        List<Pawn>list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }



    private Pawn mapRow(ResultSet rs) throws SQLException {
       PawnStatus status = PawnStatus.valueOf(rs.getString("status"));
        Date pawnDateSql = rs.getDate("pawn_date");
        Date returnDateSql = rs.getDate("return_date");
        Timestamp updated = rs.getTimestamp("updated_at");

        Pawn pawn = new Pawn(
                rs.getInt("id"),
                rs.getString("profile_id"),
                rs.getInt("article_id"),
                rs.getInt("cliente_id"),
                rs.getInt("amount"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("weight_grams"),
                rs.getInt("installment_count"),
                rs.getInt("installments_paid"),
                rs.getInt("installments_missed"),

                pawnDateSql != null ? pawnDateSql.toLocalDate() : null,
                returnDateSql != null ? returnDateSql.toLocalDate() : null,

                status,
                rs.getString("notes"),
                updated != null ? updated.toLocalDateTime():null);

        // Campos del JOIN (pueden ser null si no se hizo JOIN)
        try {
            pawn.setProfileName(rs.getString("profile_name"));
            pawn.setArticleName(rs.getString("article_name"));
            pawn.setClientName(rs.getString("cliente_name"));
        } catch (SQLException ignored) {
            // Columnas no presentes en esta query
        }

        return pawn;
    }

    @FunctionalInterface
    private interface SqlSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

}