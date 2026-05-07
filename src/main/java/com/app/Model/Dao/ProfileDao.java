package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.domain.Profile;
import com.app.Model.Enum.RolUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfileDao {

    public Profile findById(String id) throws SQLException{
        String sql= "SELECT id, email, full_name,rol, active " +
                "FROM public.profile " +
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
    public List<Profile> findAll() throws SQLException{
        String sql= "SELECT id, email, full_name,rol, active FROM public.profile ORDER BY full_name ASC";
        List<Profile> list = new ArrayList<>();
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()){

            while (rs.next()){
            list.add(mapRow(rs));
            }
        }
        return list;
    }
    public boolean updateActive(String id,boolean active) throws SQLException{
        String sql= "UPDATE public.profile SET active = ?, updated_at = NOW() WHERE id = ?::uuid";
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setBoolean(1, active);
            ps.setString(2, id);
            return ps.executeUpdate()> 0;
        }
    }
    public Profile save(Profile profile) throws SQLException {
        String sql = "INSERT INTO public.profile (id, email, full_name, rol, active) VALUES (?::uuid, ?, ?, ?::role_user, ?) RETURNING created_at, updated_at";
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, profile.getId());
            ps.setString(2, profile.getEmail());
            ps.setString(3, profile.getFullName());
            ps.setString(4, profile.getRol().name());
            ps.setBoolean(5, profile.isActive());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Update any metadata if needed
                }
            }
        }
        return profile;
    }

    public boolean update(Profile profile) throws SQLException {
        String sql= "UPDATE public.profile SET full_name = ?, rol = ?::role_user, updated_at = NOW() WHERE id = ?::uuid";
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, profile.getFullName());
            ps.setString(2, profile.getRol().name());
            ps.setString(3, profile.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM public.profile WHERE id = ?::uuid";
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Profile mapRow(ResultSet rs) throws SQLException{
        return new Profile(
                rs.getString("id"),
                rs.getString("email"),
                rs.getString("full_name"),
                RolUser.valueOf(rs.getString("rol")),
                rs.getBoolean("active")
        );
    }
}
