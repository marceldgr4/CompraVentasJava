package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.Interface.SqlSetter;
import com.app.Model.domain.Purchase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PurchaseDao {

    private static final String SELECT_COLS = """
             p.id, p.profile_id, p.cliente_id, p.article_id,
            p.purchase_price, p.purchase_date, p.notes,
            CONCAT(c.first_name, ' ', COALESCE(c.last_name, '')) AS cliente_name,
            a.name_article AS article_name,
            pr.full_name   AS profile_name
            
            """;

    private static final String FORM_JOINS = """
            FROM public.purchases p
            LEFT JOIN public.articles a ON p.article_id = a.id
            LEFT JOIN public.clientes c ON p.cliente_id = c.id
            LEFT JOIN public.profile pr ON p.profile_id = pr.id
            """;


    public List<Purchase> findAll() throws SQLException{
        String sql = "SELECT"+SELECT_COLS + FORM_JOINS+
                "ORDER BY p.purchase_date DESC LIMIT 1";
        return executeList(sql,ps -> {});
    }

    public List<Purchase> findByProfile(String profileId) throws SQLException {
        String sql = "SELECT"+ SELECT_COLS + FORM_JOINS+
                "WHERE p.profile_id = ?::uuid"+
                "ORDER BY p.purchase_date DESC";
        return executeList(sql, ps-> ps.setString(1, profileId));
    }

    public List<Purchase> findByDateRange(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT"+ SELECT_COLS+ FORM_JOINS+
                "WHERE p.purchase_date BETWEEN ? AND ?"+
                "ORDER BY p.purchase_date DESC";
        return executeList(sql,ps -> {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
        });
    }

    public Optional<Purchase> findById(int id) throws SQLException {
        String sql = "SELECT"+ SELECT_COLS+ FORM_JOINS+
                "WHERE p.purchase_id = ?";
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }
    //-----create----//
    public Purchase save(Purchase purchase) throws SQLException {
        String sql = """
                INSERT INTO public.purchases(profile_id, cliente_id, article_id, purchase_price, notes)
                VALUES (?::uuid, ?, ?, ?, ?)
                RETURNING id, purchase_date
                """;
        try (Connection connection = ConnectionPool.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)){
            setParams(ps, purchase);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    purchase.setId(rs.getInt("id"));
                    Timestamp ts = rs.getTimestamp("purchase_date");
                    if (ts != null) purchase.setPurchaseDate(LocalDate.from(ts.toLocalDateTime()));
                }
            }
        }
        return purchase;
    }

    /** Versión transaccional para usar dentro de una transacción JDBC existente. */
    public Purchase save(Connection con, Purchase purchase) throws SQLException {
        String sql = """
                INSERT INTO public.purchases(profile_id, cliente_id, article_id, purchase_price, notes)
                VALUES (?::uuid, ?, ?, ?, ?)
                RETURNING id, purchase_date
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            setParams(ps, purchase);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    purchase.setId(rs.getInt("id"));
                    Timestamp ts = rs.getTimestamp("purchase_date");
                    if (ts != null) purchase.setPurchaseDate(LocalDate.from(ts.toLocalDateTime()));
                }
            }
        }
        return purchase;
    }


    //>>>>HELP<<<<<

  private List<Purchase> executeList(String sql, SqlSetter setter) throws SQLException {
        List<Purchase> list = new ArrayList<>();
        try(Connection con = ConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
            setter.set(ps);
            try( ResultSet rs = ps.executeQuery()){
                while(rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
  }

  private void setParams(PreparedStatement ps, Purchase purchase) throws SQLException {
        ps.setString(1, purchase.getProfileId());
        if (purchase.getClientId() > 0) ps.setInt(2, purchase.getClientId());
        else ps.setNull(2, Types.INTEGER);
        ps.setInt(3, purchase.getArticleId());
        ps.setBigDecimal(4, purchase.getPurchasePrice());
        ps.setString(5, purchase.getNotes());
  }
    private Purchase mapRow(ResultSet rs) throws SQLException {
        Purchase p = new Purchase();
        p.setId(rs.getInt("id"));
        p.setProfileId(rs.getString("profile_id"));
        p.setClientId(rs.getInt("cliente_id"));
        p.setArticleId(rs.getInt("article_id"));
        p.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        Timestamp ts = rs.getTimestamp("purchase_date");
        if (ts != null) p.setPurchaseDate(LocalDate.from(ts.toLocalDateTime()));
        p.setNotes(rs.getString("notes"));
        p.setClienteName(rs.getString("cliente_name"));
        p.setArticleName(rs.getString("article_name"));
        p.setProfileName(rs.getString("profile_name"));
        return p;
    }

}
