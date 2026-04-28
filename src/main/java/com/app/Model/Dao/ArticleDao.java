package com.app.Model.Dao;


import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.domain.Article;
import com.app.Model.domain.ArticleCategory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArticleDao {

    public List<Article> findAll() throws SQLException {
        String sql = """
                SELECT id, cliente_id, name_article, description, category, amount, price,created_at,updated_at
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
    public static Optional<Article> findById(int id) throws SQLException {
        String sql = """
                SELECT id,cliente_id, name_article, description,category, amount, price,created_at,updated_at
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
                SELECT id,cliente_id, name_article, description,category, amount, price,created_at,updated_at
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

public List<Article>findAvailable() throws SQLException {
        String sql = """
                SELECT *
                FROM public.articles
                WHERE amount > 0
                ORDER BY name_article ASC;
                """;
        List<Article> list = new ArrayList<>();
        try (Connection con = ConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()){
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return  list;
        }
}
    // -------------------------------------------------------
    // CREATE — guardar artículo nuevo
    // -------------------------------------------------------
    public Article save(Article article) throws SQLException {
        String sql = """
            INSERT INTO public.articles(cliente_id, name_article, description,category, 
                                        amount, price, created_at, updated_at)
            VALUES (?, ?, ?, ?:: article_category, ?,?)
            RETURNING id,created_at, updated_at
            """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, article.getClienteId());
            ps.setString(2, article.getNameArticle());
            ps.setString(3, article.getDescription());
            ps.setString(4, article.getCategory().name());
            ps.setInt(5, article.getAmount());
            ps.setBigDecimal(6, article.getPrice());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                article.setId(rs.getInt("id"));
                Timestamp updated_at = rs.getTimestamp("updated_at");
                Timestamp created_at = rs.getTimestamp("created_at");
               if(created_at !=null) article.setCreatedAt(created_at.toLocalDateTime());
               if(updated_at !=null) article.setUpdatedAt(updated_at.toLocalDateTime());
            }
        }
        return article;
    }

    // -------------------------------------------------------
    // UPDATE — actualizar artículo completo (solo Admin)
    // -------------------------------------------------------
    public boolean update(Article article) throws SQLException {
        String sql = """
                UPDATE public.articles
                SET name_article = ?, 
                amount = ?, 
                category = ?::article_category,
                price = ?, 
                
                WHERE id = ?
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, article.getNameArticle());
            ps.setInt(2, article.getAmount());
            ps.setString(3, article.getCategory().name());
            ps.setBigDecimal(4, article.getPrice());
            ps.setInt(5, article.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateBasicFields(int id, String nameArticle, String description,
                                     ArticleCategory category) throws SQLException {
        String sql = """
                UPDATE public.articles
                SET name_article = ?,
                description = ?,
                category = ?::article_category,
            
                WHERE id = ?
        """;
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, nameArticle);
            ps.setString(2, description);
            ps.setString(3, category.name());
            ps.setInt(4,id);
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

    /**
     * Actualiza la cantidad de stock de un artículo.
     */
    public boolean updateAmount(Connection conn, int id, int newAmount) throws SQLException {
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
    public boolean updateAmount(int id, int newAmount) throws SQLException {
        try (Connection con = ConnectionPool.getConnection()){
            return  updateAmount(con, id, newAmount);
        }
    }


    private Article mapRow(ResultSet rs) throws SQLException {
        ArticleCategory category = ArticleCategory.values()[rs.getInt("category")];
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        return new Article(
                rs.getInt("id"),
                rs.getString("name_article"),
                rs.getString("description"),
                category,
                rs.getInt("amount"),
                rs.getBigDecimal("price"),
                created != null ? created.toLocalDateTime(): null,
                updated != null ? updated.toLocalDateTime(): null
            );
    }
}