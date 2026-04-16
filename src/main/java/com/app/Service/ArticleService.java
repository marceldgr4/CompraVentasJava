package com.app.Service;

import com.app.Dao.ArticleDAO;

import com.app.Model.Article;
import com.app.Model.SesionUser;

import java.math.BigDecimal;

import java.sql.SQLException;

import java.util.List;
import java.util.stream.Collectors;

public class ArticleService {
    // -------------------------------------------------------
    // Excepción propia del service
    // -------------------------------------------------------
    public static class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }
    }

    private final ArticleDAO articleDAO = new ArticleDAO();

    // -------------------------------------------------------
    // READ — lista completa del inventario
    // -------------------------------------------------------
    public List<Article> getAll() throws ServiceException {
        try {
            return articleDAO.findAll();

        } catch (SQLException e) {
            throw new ServiceException("Error loading inventory " + e.getMessage());
        }
    }

    public List<Article> getSellable() throws ServiceException {
        try{
            return articleDAO.findBySold(true)
                    .stream()
                    .filter(Article::hasStock)
                    .collect(Collectors.toList());
        }catch (SQLException e){
            throw new ServiceException("Error loading article sold " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // READ — buscar por nombre
    // -------------------------------------------------------
    public List<Article> search(String name) throws ServiceException {
        if (name == null || name.isBlank()) {
            throw new ServiceException("Name cannot be null or blank");
        }
        try {
            return articleDAO.findByName(name.trim());
        } catch (SQLException e) {
            throw new ServiceException("Error in search " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // READ — todos con stock disponible (para panel de empleado)
    // -------------------------------------------------------
    public List<Article> getAvailableForPawn() throws ServiceException {
        try {
            return articleDAO.findAll()
                    .stream()
                    .filter(Article::hasStock)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error loading inventory for employee " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // CREATE — solo Admin puede crear artículos
    // -------------------------------------------------------

    public Article create(Article article) throws ServiceException {
        requireAdmin("create new articles");
        validateArticle(article);
        try {
            return articleDAO.save(article);
        } catch (SQLException e) {
            throw new ServiceException("Error creating article " + e.getMessage());
        }
    }

    private void validateArticle(Article article) throws ServiceException {
        if (article.getNameArticle() == null || article.getNameArticle().isBlank()) {
            throw new ServiceException("Name article cannot be null or blank");
        }
        if (article.getNameArticle().length() > 255) {
            throw new ServiceException("Name article is too long");
        }
        if (article.getPrice() == null || article.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("The price must be greater than 0.  ");
        }
        if (article.getAmount() < 0) {
            throw new ServiceException("The amount cannot be negative.");
        }

    }

    private void requireAdmin(String accion) throws ServiceException {
        if (!SesionUser.getInstance().isAdmin()) {
            throw new ServiceException("You are not allowed to perform this operation" + accion + "alone the admin can created new articles or items ");
        }
    }
    // -------------------------------------------------------
    // UPDATE — editar nombre, precio y tipo (solo Admin)
    // -------------------------------------------------------
    public void edit(Article article) throws ServiceException {
        requireAdmin("edit article");
        validateArticle(article);
        try{
            boolean updated = articleDAO.updated(article);
            if (!updated) {
                throw new ServiceException("Error not find id the article "+article.getId());
            }
        } catch (SQLException e){
            throw new ServiceException("Error updating article " + e.getMessage());
        }
    }
        //---------
        // UPDATE
        //--------
    public void addStock(int articleId, int quantity) throws ServiceException {
        if (quantity <= 0) {
            throw new ServiceException("The quantity must be greater than 0.");
        }
        try{
            Article article = articleDAO.findById(articleId)
                    .orElseThrow(()-> new ServiceException(
                            "Article id" + articleId+ " not find"
                    ));
            int newAmount = article.getAmount() + quantity;
            articleDAO.updateAmount(articleId, newAmount);
        } catch (SQLException e){
            throw new ServiceException("Error updating article " + e.getMessage());
        }

    }
    public void removeStock(int articleId, int quantity) throws ServiceException {
        if (quantity <= 0) {
            throw new ServiceException("The quantity must be greater than 0.");

        }
        try {
            Article article = articleDAO.findById(articleId).orElseThrow(() -> new ServiceException(
                    "Article id" + articleId + "not find"));

            if (article.getAmount() < quantity) {
                throw new ServiceException("The out of stock. disponible " + article.getAmount() + "- Required" + quantity);

            }
            int newAmount = article.getAmount() - quantity;
            articleDAO.updateAmount(articleId, newAmount);
        } catch (SQLException e) {
            throw new ServiceException("Error remove article " + e.getMessage());
        }
    }

        public void remove(int articleId) throws ServiceException{
        requireAdmin("deleted article");
        try{
            articleDAO.findById(articleId).orElseThrow(()-> new ServiceException(
                    "Article id" + articleId+ "not find"
            ));

            boolean deleted = articleDAO.delete(articleId);
            if (!deleted) {
                throw  new ServiceException("not can deleted article."+ "can sale in procese");
            }

        }catch (SQLException e){
            throw new ServiceException("Error deleted article"+ e.getMessage());
        }
    }
}



























