package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.domain.Cliente;
import com.app.Model.Enum.ClienteStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operaciones CRUD sobre la tabla clientes.
 */
public class ClienteDao {

    // -------------------------------------------------------
    // READ — lista completa
    // -------------------------------------------------------
    public List<Cliente> getAllActive() throws SQLException {
        return findByStatus(ClienteStatus.Activo);
    }

    public List<Cliente> findAll() throws SQLException {
        String sql = """
                   SELECT id, first_name, last_name, email, phone,status, created_at,updated_at
                   FROM public.clientes
                   ORDER BY last_name ASC, first_name ASC
                   """;
        return executeListQuery(sql, ps -> {});
    }
    public List<Cliente> findByStatus(ClienteStatus status) throws SQLException {
        String sql = """
                SELECT id, first_name, last_name, email, phone,status, created_at,updated_at
                FROM public.clientes
                WHERE status =?::cliente_status 
                ORDER BY last_name ASC, first_name ASC
                """;
        return executeListQuery(sql, ps-> ps.setString(1,status.name()));
    }


    // -------------------------------------------------------
    // READ — buscar por id
    // -------------------------------------------------------
    public Optional<Cliente> findById(int id) throws SQLException {
        String sql = """
                SELECT id, first_name, last_name, email, phone,status, created_at, updated_at
                FROM public.clientes
                WHERE id=?
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // -------------------------------------------------------
    // READ — búsqueda por término (nombre, apellido o email)
    // -------------------------------------------------------
    public List<Cliente> findByTerm(String term, ClienteStatus filter) throws SQLException {
        String sql = """
               SELECT id, first_name, last_name, email, phone, status, created_at, updated_at
              FROM public.clientes
              WHERE (LOWER(last_name || ' ' || first_name) LIKE LOWER(?)
                 OR  LOWER(email) LIKE LOWER(?))
              """ + (filter != null ? "AND status = ?::cliente_status " : "") + """
              ORDER BY last_name ASC, first_name ASC
        """;
        List<Cliente> list = new ArrayList<>();
        String pattern = "%" + term + "%";
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            if(filter != null) ps.setString(3, filter.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------
    // CREATE — guardar cliente nuevo
    // -------------------------------------------------------
    public Cliente save(Cliente cliente) throws SQLException {
        String sql = """
                INSERT INTO public.clientes(first_name, last_name, email, phone,status)
                VALUES (?, ?, ?, ?,?::cliente_status)
                RETURNING id, created_at,updated_at
                """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getFirstName());
            ps.setString(2, cliente.getLastName());
            ps.setString(3, cliente.getEmail());
            ps.setString(4, cliente.getPhone());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cliente.setId(rs.getInt("id"));
                    cliente.setStatus(ClienteStatus.Activo);
                    cliente.setCreatedAt(rs.getTimestamp("created_at"));
                    cliente.setUpdatedAt(rs.getTimestamp("updated_at"));
                }
            }
        }
        return cliente;
    }

    // -------------------------------------------------------
    // UPDATE — actualizar datos del cliente
    // -------------------------------------------------------
    public boolean update(Cliente cliente) throws SQLException {
        String sql = """
                    UPDATE public.clientes
                    SET first_name = ?, last_name = ?, email = ?, phone = ?
                    WHERE id = ?
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getFirstName());
            ps.setString(2, cliente.getLastName());
            ps.setString(3, cliente.getEmail());
            ps.setString(4, cliente.getPhone());
            ps.setInt(5, cliente.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------
    // DELETE — eliminar cliente por id
    // -------------------------------------------------------
    public boolean softDelete(int id) throws SQLException {
        String sql = """
                    UPDATE public.clientes
                    SET status ='Eliminado'::cliente_status
                    WHERE id=?
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM public.clientes WHERE id = ?";
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // -------------------------------------------------------
    // Mapping Helpers
    // -------------------------------------------------------
    private List<Cliente> executeListQuery(String sql, SqlSetter setter) throws SQLException {
        List<Cliente> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));

                }
            }
        return list;

    }



    private Cliente mapRow(ResultSet rs) throws SQLException {
        ClienteStatus status = ClienteStatus.valueOf(rs.getString("status"));
        return new Cliente(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                status,
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
    }
    @FunctionalInterface
    private interface SqlSetter{
        void set(PreparedStatement ps) throws SQLException;
    }
}
