package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import Infrastructure.security.SessionManager;
import com.app.Model.domain.PawnPayment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;



public class PawnPaymentDao {

    public PawnPayment save(PawnPayment pawnPayment) throws SQLException {
      String Sql = """
              INSERT INTO public.pawn_payments(pawn_id, amount, payment_date, notes, created_by_profile_id,
              is_missed)
              VALUES (?, ?, ?, ?, ?, ?)
              RETURNING id, created_at;
              """;
              try(Connection con = ConnectionPool.getConnection();
                  PreparedStatement ps = con.prepareStatement(Sql)){
                  ps.setInt(1, pawnPayment.getPawnId());
                  ps.setBigDecimal(2, pawnPayment.getAmount());
                  ps.setDate(3, Date.valueOf(pawnPayment.getPaymentDate()));
                  ps.setString(4, pawnPayment.getNotes());
                  ps.setString(5, pawnPayment.getCreateByProfileId());
                  ps.setBoolean(6, pawnPayment.isMissed());
                  try(ResultSet rs = ps.executeQuery()){
                      if(rs.next()){
                          pawnPayment.setId(rs.getInt("id"));
                          Timestamp ts = rs.getTimestamp("created_at");
                          if(ts!=null) pawnPayment.setCreatedAt(ts.toLocalDateTime());
                      }
                  }
              }
              return pawnPayment;
    }

    public List<PawnPayment> findByPawnId(int pawnId) throws SQLException {
        String Sql = """
                  SELECT id,pawn_id, amount, payment_date, notes, created_by_profile_id, is_missed, created_at
                  FROM public.pawn_payments 
                  WHERE pawn_id = ?
                  ORDER BY payment_date DESC, id DESC;
        """;
        List<PawnPayment> list = new ArrayList<>();
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(Sql)){
            ps.setInt(1,pawnId);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }
    private  PawnPayment mapRow(ResultSet rs) throws SQLException {
        PawnPayment p = new PawnPayment();
        p.setId(rs.getInt("id"));
        p.setPawnId(rs.getInt("pawn_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        Date paymentDate = rs.getDate("payment_date");
        if(paymentDate!=null) p.setPaymentDate(paymentDate.toLocalDate());
        p.setNotes(rs.getString("notes"));
        p.setCreateByProfileId(rs.getString("created_by_profile_id"));
        p.setMissed(rs.getBoolean("is_missed"));
        Timestamp ts = rs.getTimestamp("created_at");
        if(ts!=null) p.setCreatedAt(ts.toLocalDateTime());
        return p;

    }
}
