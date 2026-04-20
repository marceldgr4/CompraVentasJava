package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.domain.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteDao {

    public List<Cliente> findAll() throws Exception {
        String sql = """ 
                   SELECT *
                   FROM public.clientes
                   ORDER BY last_name ASC, first_name ASC
                   """;
        List<Cliente> list = new ArrayList<>();
        try(Connection con = ConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()) list.add(mapRow(rs));
        }
    return list;
    }

    public Optional<Object> findById(int id) throws Exception {
        String sql = """
                SELECT *
                FROM public.clientes
                WHERE id = ?
        """;
        try(Connection con = ConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, id);
            try(ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Cliente> findByTerm(String term) throws Exception {
        String sql = """
                SELECT *
                FROM public.clientes
                WHERE LOWER(last_name || ''|| first_name) LIKE LOWER(?)
                OR LOWER(email) LIKE LOWER(?)
                ORDER BY last_name ASC, first_name ASC
        """;
        List<Cliente> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + term + "%");
            ps.setString(2, "%" + term + "%");
            ps.setString(3, "%" + term + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));

            }
        }
        return list;
    }
        public Cliente save(Cliente cliente) throws SQLException{
            String sql = """
                    INSERT INTO public.clientes(
                    first_name, last_name, email,phone,
                    VALUE(?,?,?,?)
                    RETURNING *
                    """;
            try(Connection con = ConnectionPool.getConnection();
            PreparedStatement ps= con.prepareStatement(sql)) {
                ps.setString(1, cliente.getFirstName());
                ps.setString(2, cliente.getLastName());
                ps.setString(3, cliente.getEmail());
                ps.setString(4, cliente.getPhone());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cliente.setId(rs.getInt("id"));
                        Timestamp timestamp = rs.getTimestamp("created_at");
                        if (timestamp != null) cliente.setCreated_ad(timestamp.toLocalDateTime());
                    }
                }
            }
            return cliente;
        }
        public boolean update(Cliente cliente) throws SQLException{
        String sql = """
                    UPDATE public.clientes
                    SET first_name = ?, last_name = ?, email = ?, phone = ?
                    WHERE id = ?
                    
        """;
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getFirstName());
            ps.setString(2, cliente.getLastName());
            ps.setString(3, cliente.getEmail());
            ps.setString(4, cliente.getPhone());
            ps.setInt(5, cliente.getId());
            return ps.executeUpdate() > 0;
        }
    }
    public boolean delete(int cliente) throws SQLException{
        String sql = """
                    DELETE FROM public.clientes
                    WHERE id = ?
                   """;
        try(Connection con = ConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cliente);
            return ps.executeUpdate() > 0;
        }
    }
    private Cliente mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created_at");
        return new Cliente(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                timestamp != null ? timestamp : null
        );
    }
}
