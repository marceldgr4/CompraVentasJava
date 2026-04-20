package com.app.Repositories.impl;

import com.app.Model.Dao.ArticleDao;
import com.app.Model.domain.Article;
import com.app.Repositories.ArticleRepository;

import java.util.List;
import java.util.Optional;

public class ArticleRespositoryImpl implements ArticleRepository {
    private final ArticleDao articleDao = new ArticleDao();

    @Override
    public List<Article> findAll() throws Exception {
        return List.of();
    }

    @Override
    public Optional<Article> findById(long id) throws Exception {
        return Optional.empty();
    }

    @Override
    public List<Article> findByName(String name_article) throws Exception {
        return List.of();
    }

    @Override
    public List<Article> findBySold(String sold) throws Exception {
        return List.of();
    }

    @Override
    public Article save(Article article) throws Exception {
        return null;
    }

    @Override
    public boolean update(Article article) throws Exception {
        return false;
    }

    @Override
    public boolean delete(int id) throws Exception {
        return false;
    }

    @Override
    public boolean updateAmount(int id, int newAmount) throws Exception {
        return false;
    }
}
