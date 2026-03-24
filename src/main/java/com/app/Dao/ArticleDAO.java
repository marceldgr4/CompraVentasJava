package com.app.Dao;


import com.app.Model.Article;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArticleDAO {

    public List<Article> findAll() throws SQLException{
        String sql = """
                select * 
                from public.articles
                order by id asc;
                """;
        List<Article> list = new ArrayList<>();
        try
            ( Connection con = ConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                list.add(mapRow(rs));
            }
        }
        return list;

    }

    // -------------------------------------------------------
    // READ — buscar por id
    // -------------------------------------------------------
public Optional<Article> findById(int id) throws SQLException {
        String sql = """
                select *
                from public.articles
                where id = ?;
        """;
        try (Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

        }
        return Optional.empty();
    }
// -------------------------------------------------------
    // READ — buscar por nombre (búsqueda parcial)
    // -------------------------------------------------------
    public List<Article> findByName(String name) throws SQLException {
        String sql = """
                select *
                from public.articles
                where lower(name_article) like lower(?);
                order by name_article asc
        """;
        List<Article> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, "%" + name.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }
    // -------------------------------------------------------
    // CREATE — guardar artículo nuevo
    // -------------------------------------------------------
    public Article save(Article article) throws SQLException {
        String sql = """
                insert into public.articles(name_article,amount, price,sold) 
                values(?,?,?,?);
                returning id, update_at
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, article.getNameArticle());
            ps.setInt(2, article.getAmount());
            ps.setBigDecimal(3, article.getPrice());
            ps.setBoolean(4,article.isSold());

            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                article.setId(rs.getInt("id"));
                article.setUpdatedAt(rs.getTimestamp("updated_at")
                .toLocalDateTime());
            }
        }
        return article;
    }
    // -------------------------------------------------------
    // UPDATE — actualizar artículo completo (solo Admin)
    // -------------------------------------------------------
    public boolean update(Article article) throws SQLException {
        String sql = """
                update public.articles
                set name_article = ?, 
                amount = ?, 
                price = ?, 
                sold = ?
                update_at = now()
                where id = ?
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, article.getNameArticle());
            ps.setInt(2, article.getAmount());
            ps.setBigDecimal(3, article.getPrice());
            ps.setBoolean(4, article.isSold());
            ps.setInt(5, article.getId());
            return ps.executeUpdate() > 0;
        }
    }
        public boolean delete(int id) throws SQLException {
        String sql = " delete from public.articles where id = ?";

        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1,id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Article> findBySold(boolean sold) throws SQLException {
        String sql = """
                select *
                from public.articles
                where sold = ?;
                order by name_article asc
        """;
        List<Article> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setBoolean(1, sold);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Article mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("updated_at");
        return new Article(
                rs.getInt("id"),
                rs.getString("name_article"),
                rs.getInt("amount"),
                rs.getBigDecimal("price"),
                rs.getBoolean("sold"),
                ts !=null ? ts.toLocalDateTime(): null
        );
    }

    public boolean updateAmount(int id, int newAmount)  throws SQLException{
        String sql = """
                update public.articles
                set amount = ?;
                update_at = now()
                where id = ?;
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, newAmount);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }
}