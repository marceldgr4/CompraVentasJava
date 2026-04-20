package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.domain.Profile;
import com.app.Model.domain.RolUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfileDao {

    public Profile findById(String id) throws SQLException{
        String sql= "SELECT id, full_name,rol, active " +
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
        String sql= "SELECT id, full_name,rol, active FROM public.profile ORDER BY full_name ASC";
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
    private Profile mapRow(ResultSet rs) throws SQLException{
        return new Profile(
                rs.getString("id"),
                rs.getString("full_name"),
                RolUser.valueOf(rs.getString("rol")),
                rs.getBoolean("active")
        );
    }
}
