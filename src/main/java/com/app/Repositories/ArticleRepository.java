package com.app.Repositories;

import com.app.Model.domain.Article;
import com.app.Model.Enum.ArticleCategory;
import com.app.Model.Enum.SourceType;
import com.app.Model.Enum.ItemState;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
    List<Article> findAll() throws Exception;
    Optional<Article> findById(long id) throws Exception;
    List<Article> findByName(String name) throws Exception;
    List<Article> findBySold(String sold) throws Exception;
    Article save(Article article) throws Exception;

    boolean update(Article article) throws Exception;
    boolean update(int id, String name, String description, ArticleCategory category, SourceType source, ItemState state) throws Exception;
    boolean delete(int id) throws Exception;
    boolean updateAmount(int id, int newAmount) throws Exception;
    boolean updatePrice(int id, BigDecimal newPrice) throws Exception;

}

