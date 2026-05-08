package com.app.Model.Dao;

import com.app.Model.domain.PawnPayment;

import java.sql.*;
import java.util.List;

public class PawnPaymentDao extends BaseDao<PawnPayment> {

    public PawnPayment save(PawnPayment pawnPayment) throws SQLException {
        String sql = """
                INSERT INTO public.pawn_payments(pawn_id, amount, payment_date, notes, created_by_employee_id, is_missed)
                VALUES (?, ?, ?, ?, ?::uuid, ?)
                RETURNING id, created_at
                """;
        return executeInsert(sql, ps -> {
            ps.setInt(1, pawnPayment.getPawnId());
            ps.setBigDecimal(2, pawnPayment.getAmount());
            ps.setDate(3, Date.valueOf(pawnPayment.getPaymentDate()));
            ps.setString(4, pawnPayment.getNotes());
            ps.setString(5, pawnPayment.getCreateByEmployeeId());
            ps.setBoolean(6, pawnPayment.isMissed());
        }, rs -> {
            try {
                pawnPayment.setId(rs.getInt("id"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) pawnPayment.setCreatedAt(ts.toLocalDateTime());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<PawnPayment> findByPawnId(int pawnId) throws SQLException {
        String sql = """
                  SELECT id, pawn_id, amount, payment_date, notes, created_by_employee_id, is_missed, created_at
                  FROM public.pawn_payments 
                  WHERE pawn_id = ?
                  ORDER BY payment_date DESC, id DESC
                """;
        return executeList(sql, ps -> ps.setInt(1, pawnId), PawnPaymentDao::mapRow);
    }

    private static PawnPayment mapRow(ResultSet rs) throws SQLException {
        PawnPayment p = new PawnPayment();
        p.setId(rs.getInt("id"));
        p.setPawnId(rs.getInt("pawn_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        Date paymentDate = rs.getDate("payment_date");
        if (paymentDate != null) p.setPaymentDate(paymentDate.toLocalDate());
        p.setNotes(rs.getString("notes"));
        p.setCreateByEmployeeId(rs.getString("created_by_employee_id"));
        p.setMissed(rs.getBoolean("is_missed"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        return p;
    }
}
