package com.app.Model.Dao;


import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.domain.Article;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArticleDAO {

    public List<Article> findAll() throws SQLException {
        String sql = """
                SELECT id, cliente_id, name_article, description, amount, price,sold,updated_at
                FROM public.articles
                ORDER BY name_article ASC 
                """;
        List<Article> list = new ArrayList<>();
        try
                (Connection con = ConnectionPool.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
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
                SELECT id,cliente_id, name_article, description, amount, price,sold,updated_at
                FROM public.articles
                WHERE id = ?;
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
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
                SELECT id,cliente_id, name_article, description, amount, price,sold,updated_at
                FROM public.articles
                WHERE LOWER(name_article) LIKE lower(?)
                ORDER BY name_article ASC
        """;
        List<Article> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
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
                INSERT INTO public.articles(cliente_id,name_article,description, amount, price,sold) 
                VALUES (?, ?, ?, ?,?,?)
                RETURNING id, updated_at
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, article.getCliente_id());
            ps.setString(2, article.getNameArticle());
            ps.setString(3, article.getDescription());
            ps.setInt(4, article.getAmount());
            ps.setBigDecimal(5, article.getPrice());
            ps.setBoolean(6, article.isSold());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
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
    public boolean updated(Article article) throws SQLException {
        String sql = """
                UPDATE public.articles
                SET name_article = ?, 
                amount = ?, 
                price = ?, 
                sold = ?,
                updated_at = NOW()
                WHERE id = ?
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
        String sql = " DELETE FROM public.articles WHERE id = ?";

        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateAmount(int id, int newAmount) throws SQLException {
        String sql = """
                UPDATE public.articles
                SET amount =  ?, updated_at = NOW()
                WHERE id = ?;
                
                """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newAmount);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        }
    }

    public List<Article> findBySold(boolean sold) throws SQLException {
        String sql = """
                SELECT id,cliente_id, name_article, description, amount, price,sold,updated_at
                FROM public.articles
                WHERE sold = ?
                ORDER BY name_article ASC
        """;
        List<Article> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
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
                rs.getInt("cliente_id"),
                rs.getString("name_article"),
                rs.getString("description"),
                rs.getInt("amount"),
                rs.getBigDecimal("price"),
                rs.getBoolean("sold"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}