package com.app.Model.Dao;

import com.app.Model.domain.Employee;
import com.app.Model.Enum.RolUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EmployeeDao extends BaseDao<Employee> {

    private static final String SELECT_COLS = "id, email, full_name, rol, active";

    public Optional<Employee> findById(String id) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.employees WHERE id = ?::uuid";
        return executeSingle(sql, ps -> ps.setString(1, id), EmployeeDao::mapRow);
    }

    public List<Employee> findAll() throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.employees ORDER BY full_name ASC";
        return executeList(sql, null, EmployeeDao::mapRow);
    }

    public boolean updateActive(String id, boolean active) throws SQLException {
        String sql = "UPDATE public.employees SET active = ?, updated_at = NOW() WHERE id = ?::uuid";
        return executeUpdate(sql, ps -> {
            ps.setBoolean(1, active);
            ps.setString(2, id);
        });
    }

    public Employee save(Employee employee) throws SQLException {
        String sql = "INSERT INTO public.employees (id, email, full_name, rol, active) VALUES (?::uuid, ?, ?, ?::role_user, ?) RETURNING created_at, updated_at";
        return executeInsert(sql, ps -> {
            ps.setString(1, employee.getId());
            ps.setString(2, employee.getEmail());
            ps.setString(3, employee.getFullName());
            ps.setString(4, employee.getRol().name());
            ps.setBoolean(5, employee.isActive());
        }, null);
    }

    public boolean update(Employee employee) throws SQLException {
        String sql = "UPDATE public.employees SET full_name = ?, rol = ?::role_user, updated_at = NOW() WHERE id = ?::uuid";
        return executeUpdate(sql, ps -> {
            ps.setString(1, employee.getFullName());
            ps.setString(2, employee.getRol().name());
            ps.setString(3, employee.getId());
        });
    }

    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM public.employees WHERE id = ?::uuid";
        return executeUpdate(sql, ps -> ps.setString(1, id));
    }

    private static Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getString("id"),
                rs.getString("email"),
                rs.getString("full_name"),
                safeEnum(RolUser.class, rs.getString("rol"), RolUser.Empleado),
                rs.getBoolean("active")
        );
    }
}
