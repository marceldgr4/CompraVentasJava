package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.domain.Employee;
import com.app.Model.Enum.RolUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDao {

    public Employee findById(String id) throws SQLException{
        String sql= "SELECT id, email, full_name, rol, active " +
                "FROM public.employees " +
                "WHERE id = ?::uuid";

        try(Connection con = ConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    public List<Employee> findAll() throws SQLException{
        String sql= "SELECT id, email, full_name, rol, active FROM public.employees ORDER BY full_name ASC";
        List<Employee> list = new ArrayList<>();
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()){

            while (rs.next()){
            list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean updateActive(String id, boolean active) throws SQLException{
        String sql= "UPDATE public.employees SET active = ?, updated_at = NOW() WHERE id = ?::uuid";
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setBoolean(1, active);
            ps.setString(2, id);
            return ps.executeUpdate()> 0;
        }
    }

    public Employee save(Employee employee) throws SQLException {
        String sql = "INSERT INTO public.employees (id, email, full_name, rol, active) VALUES (?::uuid, ?, ?, ?::role_user, ?) RETURNING created_at, updated_at";
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, employee.getId());
            ps.setString(2, employee.getEmail());
            ps.setString(3, employee.getFullName());
            ps.setString(4, employee.getRol().name());
            ps.setBoolean(5, employee.isActive());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Update any metadata if needed
                }
            }
        }
        return employee;
    }

    public boolean update(Employee employee) throws SQLException {
        String sql= "UPDATE public.employees SET full_name = ?, rol = ?::role_user, updated_at = NOW() WHERE id = ?::uuid";
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, employee.getFullName());
            ps.setString(2, employee.getRol().name());
            ps.setString(3, employee.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM public.employees WHERE id = ?::uuid";
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException{
        return new Employee(
                rs.getString("id"),
                rs.getString("email"),
                rs.getString("full_name"),
                RolUser.valueOf(rs.getString("rol")),
                rs.getBoolean("active")
        );
    }
}
