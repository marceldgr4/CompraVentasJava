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

public class SaleDao extends BaseDao<Sale> {

    public Sale save(Sale sale) throws SQLException {
        DataBaseManeger.runInTransaction(connection -> {
            String sql = """
                    INSERT INTO public.sales(employee_id, cliente_id, cliente_nombre_anon, sale_date)
                    VALUES (?::uuid, ?, ?, ?)
                    RETURNING id
                    """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, sale.getEmployeeId());
                if (sale.getClienteId() > 0) ps.setInt(2, sale.getClienteId());
                else ps.setNull(2, Types.INTEGER);
                ps.setString(3, sale.getClienteNombreAnon());
                ps.setTimestamp(4, Timestamp.valueOf(sale.getSaleDate()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) sale.setId(rs.getInt("id"));
                }
            }

            String sqlDetail = """
                    INSERT INTO public.sales_details(sale_id, article_id, amount, unit_price)
                    VALUES (?, ?, ?, ?)
                    RETURNING id
                    """;
            for (SalesDetail detail : sale.getDetails()) {
                detail.setSaleId(sale.getId());
                try (PreparedStatement psDetail = connection.prepareStatement(sqlDetail)) {
                    psDetail.setInt(1, detail.getSaleId());
                    psDetail.setInt(2, detail.getArticleId());
                    psDetail.setInt(3, detail.getAmount());
                    psDetail.setBigDecimal(4, detail.getUnitPrice());
                    try (ResultSet rs = psDetail.executeQuery()) {
                        if (rs.next()) detail.setId(rs.getInt("id"));
                    }
                }
            }
        });
        return sale;
    }

    private static final String SELECT_COLS = "id, employee_id, cliente_id, cliente_nombre_anon, sale_date, notes";

    public Optional<Sale> findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.sales WHERE id = ?";
        return executeSingle(sql, ps -> ps.setInt(1, id), SaleDao::mapRow);
    }

    public List<Sale> findAll() throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.sales ORDER BY id DESC";
        return executeList(sql, null, rs -> {
            Sale sale = mapRow(rs);
            try (Connection con = ConnectionPool.getConnection()) {
                sale.setDetails(findDetailsBySaleId(con, sale.getId()));
            }
            return sale;
        });
    }

    public List<Sale> findClientes(int clienteId) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.sales WHERE cliente_id = ? ORDER BY sale_date DESC";
        return findByParam(sql, ps -> ps.setInt(1, clienteId));
    }

    public List<Sale> findByEmployee(String employeeId) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.sales WHERE employee_id = ?::uuid ORDER BY sale_date DESC";
        return findByParam(sql, ps -> ps.setString(1, employeeId));
    }

    public List<Sale> findByDateRange(LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.sales WHERE sale_date::date BETWEEN ? AND ? ORDER BY sale_date DESC";
        return executeList(sql, ps -> {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
        }, rs -> {
            Sale sale = mapRow(rs);
            try (Connection con = ConnectionPool.getConnection()) {
                sale.setDetails(findDetailsBySaleId(con, sale.getId()));
            }
            return sale;
        });
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM public.sales WHERE id = ?";
        return executeUpdate(sql, ps -> ps.setInt(1, id));
    }

    private List<Sale> findByParam(String sql, SqlSetter setter) throws SQLException {
        return executeList(sql, setter, rs -> {
            Sale sale = mapRow(rs);
            try (Connection con = ConnectionPool.getConnection()) {
                sale.setDetails(findDetailsBySaleId(con, sale.getId()));
            }
            return sale;
        });
    }

    private List<SalesDetail> findDetailsBySaleId(Connection connection, int saleId) throws SQLException {
        String sql = "SELECT * FROM public.sales_details WHERE sale_id = ?";
        List<SalesDetail> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SalesDetail(
                            rs.getInt("id"),
                            rs.getInt("sale_id"),
                            rs.getInt("article_id"),
                            rs.getInt("amount"),
                            rs.getBigDecimal("unit_price")
                    ));
                }
            }
        }
        return list;
    }

    private static Sale mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("sale_date");
        Sale sale = new Sale(
                rs.getInt("id"),
                rs.getString("employee_id"),
                rs.getInt("cliente_id"),
                ts != null ? ts.toLocalDateTime() : null
        );
        sale.setClienteNombreAnon(rs.getString("cliente_nombre_anon"));
        return sale;
    }
}
