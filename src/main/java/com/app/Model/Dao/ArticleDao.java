package com.app.Model.Dao;

import Infrastructure.DataBase.ConnectionPool;
import com.app.Model.Enum.ArticleCategory;
import com.app.Model.Enum.ItemState;
import com.app.Model.Enum.SourceType;
import com.app.Model.domain.Article;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArticleDao extends BaseDao<Article> {

    private static final String SELECT_COLS = """
            id, cliente_id, name_article, description, category, source_type, item_state, amount, price, purchase_price,
            created_at, updated_at
            """;

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<Article> findAll() throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.articles ORDER BY name_article ASC";
        return executeList(sql, null, ArticleDao::mapRow);
    }

    public Optional<Article> findById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.articles WHERE id = ?";
        return executeSingle(sql, ps -> ps.setInt(1, id), ArticleDao::mapRow);
    }

    public List<Article> findByName(String name) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.articles " +
                "WHERE LOWER(name_article) LIKE LOWER(?) ORDER BY name_article ASC";
        return executeList(sql, ps -> ps.setString(1, "%" + name.toLowerCase() + "%"), ArticleDao::mapRow);
    }

    public List<Article> findAvailable() throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.articles " +
                "WHERE amount > 0 ORDER BY name_article ASC";
        return executeList(sql, null, ArticleDao::mapRow);
    }

    public List<Article> findBySourceType(SourceType sourceType) throws SQLException {
        String sql = "SELECT " + SELECT_COLS + " FROM public.articles " +
                "WHERE source_type = ?::source_type ORDER BY name_article ASC";
        return executeList(sql, ps -> ps.setString(1, sourceType.name()), ArticleDao::mapRow);
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    public Article save(Article article) throws SQLException {
        String sql = """
                INSERT INTO public.articles(
                    cliente_id, name_article, description, category,
                    source_type, item_state, amount, price, purchase_price)
                VALUES (?, ?, ?, ?::article_category, ?::source_type, ?::item_state, ?, ?, ?)
                RETURNING id, created_at, updated_at
                """;
        return executeInsert(sql, ps -> setArticleParams(ps, article), rs -> {
            try {
                article.setId(rs.getInt("id"));
                Timestamp created = rs.getTimestamp("created_at");
                Timestamp updated = rs.getTimestamp("updated_at");
                if (created != null) article.setCreatedAt(created.toLocalDateTime());
                if (updated != null) article.setUpdatedAt(updated.toLocalDateTime());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Article save(Connection con, Article article) throws SQLException {
        // Mantenemos la versión con conexión externa para transacciones complejas
        String sql = """
                INSERT INTO public.articles(
                    cliente_id, name_article, description, category,
                    source_type, item_state, amount, price, purchase_price)
                VALUES (?, ?, ?, ?::article_category, ?::source_type, ?::item_state, ?, ?, ?)
                RETURNING id, created_at, updated_at
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            setArticleParams(ps, article);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    article.setId(rs.getInt("id"));
                    Timestamp created = rs.getTimestamp("created_at");
                    Timestamp updated = rs.getTimestamp("updated_at");
                    if (created != null) article.setCreatedAt(created.toLocalDateTime());
                    if (updated != null) article.setUpdatedAt(updated.toLocalDateTime());
                }
            }
        }
        return article;
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    public boolean update(Article article) throws SQLException {
        String sql = """
                UPDATE public.articles
                SET name_article = ?,
                    description = ?,
                    category = ?::article_category,
                    source_type = ?::source_type,
                    item_state = ?::item_state,
                    amount = ?,
                    price = ?,
                    purchase_price = ?,
                    updated_at = NOW()
                WHERE id = ?
                """;
        return executeUpdate(sql, ps -> {
            ps.setString(1, article.getNameArticle());
            ps.setString(2, article.getDescription());
            ps.setString(3, article.getCategory().name());
            ps.setString(4, sourceTypeOrDefault(article).name());
            ps.setString(5, itemStateOrDefault(article).name());
            ps.setInt(6, article.getAmount());
            ps.setBigDecimal(7, article.getPrice());
            ps.setBigDecimal(8, article.getPurchasePrice());
            ps.setInt(9, article.getId());
        });
    }

    public boolean updateAmountTransactional(Connection con, int id, int newAmount) throws SQLException {
        String sql = "UPDATE public.articles SET amount = ?, updated_at = NOW() WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newAmount);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateBasicFields(int id, String name, String description, ArticleCategory category, SourceType source, ItemState state) throws SQLException {
        String sql = """
                UPDATE public.articles
                SET name_article = ?, description = ?, category = ?::article_category,
                    source_type = ?::source_type, item_state = ?::item_state, updated_at = NOW()
                WHERE id = ?
                """;
        return executeUpdate(sql, ps -> {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setString(3, category.name());
            ps.setString(4, source.name());
            ps.setString(5, state.name());
            ps.setInt(6, id);
        });
    }

    public boolean updatePrice(int id, BigDecimal newPrice) throws SQLException {
        String sql = "UPDATE public.articles SET price = ?, updated_at = NOW() WHERE id = ?";
        return executeUpdate(sql, ps -> {
            ps.setBigDecimal(1, newPrice);
            ps.setInt(2, id);
        });
    }

    public boolean updateAmount(int id, int newAmount) throws SQLException {
        String sql = "UPDATE public.articles SET amount = ?, updated_at = NOW() WHERE id = ?";
        return executeUpdate(sql, ps -> {
            ps.setInt(1, newAmount);
            ps.setInt(2, id);
        });
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM public.articles WHERE id = ?";
        return executeUpdate(sql, ps -> ps.setInt(1, id));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private void setArticleParams(PreparedStatement ps, Article article) throws SQLException {
        if (article.getClienteId() > 0) ps.setInt(1, article.getClienteId());
        else ps.setNull(1, Types.INTEGER);
        
        ps.setString(2, article.getNameArticle());
        ps.setString(3, article.getDescription());
        ps.setString(4, article.getCategory().name());
        ps.setString(5, sourceTypeOrDefault(article).name());
        ps.setString(6, itemStateOrDefault(article).name());
        ps.setInt(7, article.getAmount());
        ps.setBigDecimal(8, article.getPrice());
        ps.setBigDecimal(9, article.getPurchasePrice());
    }

    private static Article mapRow(ResultSet rs) throws SQLException {
        ArticleCategory category = safeEnum(ArticleCategory.class, rs.getString("category"), ArticleCategory.Otro);
        SourceType sourceType = safeEnum(SourceType.class, rs.getString("source_type"), SourceType.OTRO);
        ItemState itemState = safeEnum(ItemState.class, rs.getString("item_state"), ItemState.Bueno);

        Article article = new Article(
                rs.getInt("cliente_id"),
                rs.getString("name_article"),
                rs.getString("description"),
                rs.getInt("amount"),
                rs.getBigDecimal("price"),
                category,
                sourceType,
                itemState,
                rs.getBigDecimal("purchase_price")
        );
        article.setId(rs.getInt("id"));
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null) article.setCreatedAt(created.toLocalDateTime());
        if (updated != null) article.setUpdatedAt(updated.toLocalDateTime());
        return article;
    }

    private SourceType sourceTypeOrDefault(Article article) {
        return article.getSourceType() != null ? article.getSourceType() : SourceType.OTRO;
    }

    private ItemState itemStateOrDefault(Article article) {
        return article.getItemState() != null ? article.getItemState() : ItemState.Bueno;
    }
}