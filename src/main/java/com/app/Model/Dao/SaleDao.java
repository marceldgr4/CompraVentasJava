package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import Infrastructure.DataBase.DataBaseManeger;
import com.app.Model.domain.Sale;
import com.app.Model.domain.SalesDetail;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class SaleDao {
    public Sale save(Sale sale) throws SQLException {
        DataBaseManeger.runInTransaction(connection -> {
            String sql =
                    """
                    INSERT INTO public.sales(profile_id, cliente_id,sale_date)
                    VALUES (?::uuid,?,?);
                    RETURNING id
                    """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, sale.getProfileId());
                ps.setInt(2,sale.getClienteId());
                ps.setTimestamp(3, Timestamp.valueOf(sale.getSaleDate()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sale.setId(rs.getInt("id"));
                    }
                }
            }
            String sqlDetail = """
                    INSERT INTO public.sales_details(sale_id,article_id,amount,unit_price)
                    VALUES (?,?,?,?);
                    RETURNING id
                    """;
            for(SalesDetail detail: sale.getDetails()){
                detail.setSaleId(sale.getId());
                try(PreparedStatement ps = connection.prepareStatement(sqlDetail)) {
                    ps.setInt(1, detail.getSaleId());
                    ps.setInt(2, detail.getArticleId());
                    ps.setInt(3, detail.getAmount());
                    ps.setBigDecimal(4, detail.getUnitPrice());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            detail.setId(rs.getInt("id"));
                        }
                    }
                }
            }
        });
        return sale;
    }
    public Optional<Sale> findById(int id) throws SQLException {
        String sql= """
                SELECT * FROM public.sales WHERE id = ?;
                """;
        try (Connection con = ConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapRow(rs);
                    return Optional.of(sale);
                }
            }
        }
        return Optional.empty();
    }
    public List<Sale> findAll() throws SQLException {
        String sql = """
                SELECT * 
                FROM public.sales
                order by id desc
        """;
        List<Sale> saleList = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Sale sale = mapRow(rs);
                sale.setDetails(findDetailsBySaleId(con, sale.getId()));
                saleList.add(sale);
            }
        }
        return saleList;
    }
    public List<Sale> findClientes(int clienteId)  throws SQLException {
        String sql = """
                SELECT *
                FROM public.sales
                WHERE cliente_id = ?
                order by sale_date desc
        """;
        return findByParam(sql, ps -> ps.setInt(1,clienteId));
    }
    public List<Sale> findByProfile(String profileId) throws SQLException {
        String sql = """
                SELECT *
                FROM public.sales
                WHERE profile_id = ?::uuid;
                order by sale_date desc
        """;
        return findByParam(sql, ps -> ps.setString(1,profileId));
        }


        public List<Sale> findByDateRange(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT *
                FROM public.sales
                WHERE sale_date :: date BETWEEN ? AND ?;
                order by sale date desc;
        """;

        List<Sale> saleList = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)){
             ps.setDate(1, Date.valueOf(from));
             ps.setDate(2, Date.valueOf(to));
             try (ResultSet rs = ps.executeQuery()){
                 while (rs.next()) {
                     Sale sale = mapRow(rs);
                     sale.setDetails(findDetailsBySaleId(con, sale.getId()));
                     saleList.add(sale);
                 }
             }
        }
        return saleList;
    }

    public boolean delete(int id) throws SQLException {
        String sql = """
                DELETE FROM public.sales
                WHERE id = ?
        """;
        try (Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
    private List<Sale> findByParam(String sql, SqlParamSetter setter) throws SQLException{
        List<Sale> saleList = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapRow(rs);
                    sale.setDetails(findDetailsBySaleId(con,sale.getId()));
                    saleList.add(sale);
                }
            }
        }
        return saleList;
    }

    private List<SalesDetail> findDetailsBySaleId(Connection connection, int saleId) throws SQLException {
        String sql = """
                SELECT *
                FROM public.sales_details
                WHERE sale_id = ?
        """;
        List<SalesDetail> salesDetailList = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    salesDetailList.add(new SalesDetail(
                            rs.getInt("id"),
                            rs.getInt("sale_id"),
                            rs.getInt("article_id"),
                            rs.getInt("amount"),
                            rs.getBigDecimal("unit_price")
                    ));
                }
            }
        }
        return salesDetailList;
    }
    private Sale mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("sale_date");
        return new Sale(
                rs.getInt("id"),
                rs.getString("profile_id"),
                rs.getInt("cliente_id"),
                ts !=null ? ts.toLocalDateTime(): null
        );
    }



    @FunctionalInterface
    private interface SqlParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

}
