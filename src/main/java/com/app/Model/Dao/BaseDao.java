package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Clase base para los Data Access Objects (DAO).
 * Centraliza la gestión de conexiones y operaciones comunes para reducir código duplicado.
 * 
 * @param <T> El tipo de entidad que maneja el DAO.
 */
public abstract class BaseDao<T> {

    /**
     * Interfaz funcional para configurar parámetros en un PreparedStatement.
     */
    @FunctionalInterface
    public interface SqlSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    /**
     * Interfaz funcional para mapear una fila de ResultSet a un objeto.
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /**
     * Ejecuta una consulta que retorna una lista de objetos.
     */
    protected List<T> executeList(String sql, SqlSetter setter, RowMapper<T> mapper) throws SQLException {
        List<T> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (setter != null) setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Ejecuta una consulta que retorna un único objeto opcional.
     */
    protected Optional<T> executeSingle(String sql, SqlSetter setter, RowMapper<T> mapper) throws SQLException {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (setter != null) setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.map(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Ejecuta una inserción y retorna el objeto con su ID generado.
     */
    protected T executeInsert(String sql, SqlSetter setter, Consumer<ResultSet> idSetter) throws SQLException {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (setter != null) setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && idSetter != null) {
                    idSetter.accept(rs);
                }
            }
        }
        return null; // El objeto original se modifica vía idSetter
    }

    /**
     * Ejecuta una actualización o eliminación (UPDATE/DELETE).
     */
    protected boolean executeUpdate(String sql, SqlSetter setter) throws SQLException {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (setter != null) setter.set(ps);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Helper para manejar Enums de forma segura desde la base de datos.
     */
    protected static <E extends Enum<E>> E safeEnum(Class<E> enumType, String value, E defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            for (E constant : enumType.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(value)) return constant;
            }
            return defaultValue;
        }
    }

    /**
     * Helper para limpiar números de teléfono.
     */
    protected String sanitizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[^0-9+]", "");
    }
}
