package com.app.Repositories;

import com.app.Model.domain.Article;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
    List<Article> findAll() throws Exception;
    Optional<Article> findById(long id) throws Exception;
    List<Article> findByName(String name) throws Exception;
    List<Article> findBySold(String sold) throws Exception;
    Article save(Article article) throws Exception;

    boolean update(Article article) throws Exception;
    boolean delete(int id) throws Exception;
    boolean updateAmount(int id, int newAmount) throws Exception;

}
