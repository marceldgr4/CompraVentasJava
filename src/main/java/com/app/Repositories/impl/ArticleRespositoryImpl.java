package com.app.Repositories.impl;

import com.app.Model.Dao.ArticleDao;
import com.app.Model.domain.Article;
import com.app.Repositories.ArticleRepository;
import com.app.Model.Enum.ArticleCategory;
import com.app.Model.Enum.SourceType;
import com.app.Model.Enum.ItemState;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ArticleRespositoryImpl implements ArticleRepository {
    private final ArticleDao articleDao = new ArticleDao();

    @Override
    public List<Article> findAll() throws Exception {
        return articleDao.findAll();
    }

    @Override
    public Optional<Article> findById(long id) throws Exception {
        return articleDao.findById((int)id);
    }

    @Override
    public List<Article> findByName(String name_article) throws Exception {
        return articleDao.findByName(name_article);
    }

    @Override
    public List<Article> findBySold(String sold) throws Exception {
        return List.of();
    }

    @Override
    public Article save(Article article) throws Exception {
        return articleDao.save(article);
    }

    @Override
    public boolean update(Article article) throws Exception {
        return articleDao.update(article);
    }

    @Override
    public boolean update(int id, String name, String description, ArticleCategory category, SourceType source, ItemState state) throws Exception {
        return articleDao.updateBasicFields(id, name, description, category, source, state);
    }

    @Override
    public boolean delete(int id) throws Exception {
        return articleDao.delete(id);
    }

    @Override
    public boolean updateAmount(int id, int newAmount) throws Exception {
        return articleDao.updateAmount(id, newAmount);
    }

    @Override
    public boolean updatePrice(int id, BigDecimal newPrice) throws Exception {
        return articleDao.updatePrice(id, newPrice);
    }
}
