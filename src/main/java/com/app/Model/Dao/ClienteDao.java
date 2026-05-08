package com.app.Model.Dao;

import com.app.Model.Enum.ClienteStatus;
import com.app.Model.Enum.RegistrationType;
import com.app.Model.domain.Cliente;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class ClienteDao extends BaseDao<Cliente> {

    private static final String SELECT_COLS = """
            id, cedula, first_name, last_name, email, phone, address, city,
            status, registration_type, created_at, updated_at
            """;

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<Cliente> findAll() throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.clientes " +
                "ORDER BY last_name ASC NULLS LAST, first_name ASC";
        return executeList(sql, null, ClienteDao::mapRow);
    }

    public List<Cliente> getAllActive() throws SQLException {
        return findByStatus(ClienteStatus.Activo);
    }

    public List<Cliente> findByStatus(ClienteStatus status) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.clientes " +
                "WHERE status = ?::cliente_status " +
                "ORDER BY last_name ASC NULLS LAST, first_name ASC";
        return executeList(sql, ps -> ps.setString(1, status.name()), ClienteDao::mapRow);
    }

    public Optional<Cliente> findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.clientes WHERE id = ?";
        return executeSingle(sql, ps -> ps.setInt(1, id), ClienteDao::mapRow);
    }

    public Optional<Cliente> findByCedula(String cedula) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.clientes" +
                " WHERE cedula = ? AND status = 'Activo'::cliente_status LIMIT 1";
        return executeSingle(sql, ps -> ps.setString(1, cedula.trim()), ClienteDao::mapRow);
    }

    public List<Cliente> findByTerm(String term, ClienteStatus statusFilter) throws SQLException {
        String pattern = "%" + term + "%";
        StringBuilder sql = new StringBuilder("SELECT " + SELECT_COLS + " FROM public.clientes WHERE ");
        sql.append("(LOWER(first_name) LIKE LOWER(?) OR LOWER(COALESCE(last_name, '')) LIKE LOWER(?) OR LOWER(COALESCE(email, '')) LIKE LOWER(?) OR COALESCE(phone, '') LIKE ? OR COALESCE(cedula, '') LIKE ?) ");
        if (statusFilter != null) sql.append("AND status = ?::cliente_status ");
        sql.append("ORDER BY last_name ASC NULLS LAST, first_name ASC");

        return executeList(sql.toString(), ps -> {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            if (statusFilter != null) ps.setString(6, statusFilter.name());
        }, ClienteDao::mapRow);
    }

    public Optional<Cliente> findByPhone(String phone) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.clientes " +
                "WHERE phone = ? AND status = 'Activo'::cliente_status LIMIT 1";
        return executeSingle(sql, ps -> ps.setString(1, sanitizePhone(phone)), ClienteDao::mapRow);
    }

    public List<Cliente> findIncompleteClients() throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.clientes " +
                "WHERE registration_type = 'RAPIDO'::registration_type " +
                "AND status = 'Activo'::cliente_status " +
                "ORDER BY created_at DESC";
        return executeList(sql, null, ClienteDao::mapRow);
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    public Cliente save(Cliente cliente) throws SQLException {
        String sql = """
                INSERT INTO public.clientes (
                    cedula, first_name, last_name, email, phone, address, city, status, registration_type
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::cliente_status, ?::registration_type)
                RETURNING id, created_at, updated_at
                """;
        return executeInsert(sql, ps -> setClienteParams(ps, cliente), rs -> {
            try {
                cliente.setId(rs.getInt("id"));
                Timestamp created = rs.getTimestamp("created_at");
                Timestamp updated = rs.getTimestamp("updated_at");
                if (created != null) cliente.setCreatedAt(created.toLocalDateTime());
                if (updated != null) cliente.setUpdatedAt(updated.toLocalDateTime());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Versión transaccional para operaciones complejas. */
    public Cliente save(Connection con, Cliente cliente) throws SQLException {
        String sql = """
                INSERT INTO public.clientes (
                    cedula, first_name, last_name, email, phone, address, city, status, registration_type
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::cliente_status, ?::registration_type)
                RETURNING id, created_at, updated_at
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            setClienteParams(ps, cliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cliente.setId(rs.getInt("id"));
                    Timestamp created = rs.getTimestamp("created_at");
                    Timestamp updated = rs.getTimestamp("updated_at");
                    if (created != null) cliente.setCreatedAt(created.toLocalDateTime());
                    if (updated != null) cliente.setUpdatedAt(updated.toLocalDateTime());
                }
            }
        }
        return cliente;
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    public boolean update(Cliente cliente) throws SQLException {
        String sql = """
                UPDATE public.clientes
                SET cedula = ?, first_name = ?, last_name = ?, email = ?,
                    phone = ?, address = ?, city = ?, status = ?::cliente_status,
                    registration_type = ?::registration_type, updated_at = NOW()
                WHERE id = ?
                """;
        return executeUpdate(sql, ps -> {
            setClienteParams(ps, cliente);
            ps.setInt(10, cliente.getId());
        });
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM public.clientes WHERE id = ?";
        return executeUpdate(sql, ps -> ps.setInt(1, id));
    }

    public boolean softDelete(int id) throws SQLException {
        String sql = "UPDATE public.clientes SET status = 'Inactivo'::cliente_status, updated_at = NOW() WHERE id = ?";
        return executeUpdate(sql, ps -> ps.setInt(1, id));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private void setClienteParams(PreparedStatement ps, Cliente c) throws SQLException {
        ps.setString(1, c.getCedula() != null ? c.getCedula().trim() : null);
        ps.setString(2, c.getFirstName());
        ps.setString(3, c.getLastName());
        ps.setString(4, c.getEmail());
        ps.setString(5, sanitizePhone(c.getPhone()));
        ps.setString(6, c.getAddress());
        ps.setString(7, c.getCity());
        ps.setString(8, (c.getStatus() != null ? c.getStatus() : ClienteStatus.Activo).name());
        ps.setString(9, (c.getRegistrationType() != null ? c.getRegistrationType() : RegistrationType.COMPLETO).name());
    }

    private static Cliente mapRow(ResultSet rs) throws SQLException {
        ClienteStatus status = safeEnum(ClienteStatus.class, rs.getString("status"), ClienteStatus.Activo);
        RegistrationType regType = safeEnum(RegistrationType.class, rs.getString("registration_type"), RegistrationType.COMPLETO);

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        return new Cliente(
                rs.getInt("id"),
                rs.getString("cedula"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("city"),
                status,
                regType,
                created != null ? created.toLocalDateTime() : null,
                updated != null ? updated.toLocalDateTime() : null
        );
    }
}