package com.app.Dao;

import com.app.Model.Pawn;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PawnDao {

    private static final String SELECT_COLS= """
            Select *, a.name_article
            from public.pawn
            join public.articles a on a.id = public.article_id
            """;

    public List<Pawn> findAll() throws SQLException {
        String sql = SELECT_COLS + "order by public.pawn pawn_date desc";
        List<Pawn> list = new ArrayList<>();
        try(Connection con = ConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }
    public  List<Pawn> findByProfileId(String profileId) throws SQLException {
        String sql = SELECT_COLS + "where_public.profile_id = ?::uuid"+
                "order by public.pawn pawn_date desc";
        List<Pawn> list = new ArrayList<>();
        try(Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, profileId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Pawn mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("updated_at");
        Pawn pawn = new Pawn();
        rs.getInt("id"),
        rs.getString("profile_id"),

    }

}
