package com.app.Service;

import Infrastructure.security.SessionManager;
import com.app.Model.Dao.ArticleDao;
import com.app.Model.domain.Article;
import com.app.Service.exceptions.ServiceException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ArticleService {
    private final ArticleDao articleDAO = new ArticleDao();

    // -------------------------------------------------------
    // READ — lista completa del inventario
    // -------------------------------------------------------
    public List<Article> getAll() throws ServiceException {
        try {
            return articleDAO.findAll();

        } catch (SQLException e) {
            throw new ServiceException("Error al cargar el inventario: " + e.getMessage());
        }
    }

    public List<Article> getSellable() throws ServiceException {
        try{
            return articleDAO.findBySold(false)
                    .stream()
                    .filter(Article::hasStock)
                    .collect(Collectors.toList());
        }catch (SQLException e){
            throw new ServiceException("Error al cargar los artículos vendidos: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // READ — buscar por nombre
    // -------------------------------------------------------
    public List<Article> search(String name) throws ServiceException {
        if (name == null || name.isBlank()) {
            throw new ServiceException("El nombre no puede ser nulo o estar vacío");
        }
        try {
            return articleDAO.findByName(name.trim());
        } catch (SQLException e) {
            throw new ServiceException("Error en la búsqueda: " + e.getMessage());
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
            throw new ServiceException("Error al cargar el inventario del empleado: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // CREATE — solo Admin puede crear artículos
    // -------------------------------------------------------

    public Article create(Article article) throws ServiceException {
        requireAdmin("crear nuevos artículos");
        validateArticle(article);
        try {
            return articleDAO.save(article);
        } catch (SQLException e) {
            throw new ServiceException("Error al crear el artículo: " + e.getMessage());
        }
    }

    private void validateArticle(Article article) throws ServiceException {
        if (article.getNameArticle() == null || article.getNameArticle().isBlank()) {
            throw new ServiceException("El nombre del artículo no puede ser nulo o estar vacío");
        }
        if (article.getNameArticle().length() > 255) {
            throw new ServiceException("El nombre del artículo es demasiado largo");
        }
        if (article.getPrice() == null || article.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("El precio debe ser mayor que 0.");
        }
        if (article.getAmount() < 0) {
            throw new ServiceException("La cantidad no puede ser negativa.");
        }

    }

    private void requireAdmin(String accion) throws ServiceException {
        if (!SessionManager.isAdmin()) {
            throw new ServiceException("No tiene permisos para " + accion + ". Solo el administrador puede realizar esta operación.");
        }
    }
    // -------------------------------------------------------
    // UPDATE — editar nombre, precio y tipo (solo Admin)
    // -------------------------------------------------------
    public void edit(Article article) throws ServiceException {
        requireAdmin("editar artículo");
        validateArticle(article);
        try{
            boolean updated = articleDAO.update(article);
            if (!updated) {
                throw new ServiceException("Error: no se encontró el artículo con ID " + article.getId());
            }
        } catch (SQLException e){
            throw new ServiceException("Error al actualizar el artículo: " + e.getMessage());
        }
    }
        //---------
        // UPDATE
        //--------
    public void addStock(int articleId, int quantity) throws ServiceException {
        if (quantity <= 0) {
            throw new ServiceException("La cantidad debe ser mayor que 0.");
        }
        try{
            Article article = articleDAO.findById(articleId)
                    .orElseThrow(()-> new ServiceException(
                            "No se encontró el artículo con ID " + articleId
                    ));
            int newAmount = article.getAmount() + quantity;
            articleDAO.updateAmount(articleId, newAmount);
        } catch (SQLException e){
            throw new ServiceException("Error al actualizar el artículo: " + e.getMessage());
        }

    }
    public void removeStock(int articleId, int quantity) throws ServiceException {
        if (quantity <= 0) {
            throw new ServiceException("La cantidad debe ser mayor que 0.");

        }
        try {
            Article article = articleDAO.findById(articleId).orElseThrow(() -> new ServiceException(
                    "No se encontró el artículo con ID " + articleId));

            if (article.getAmount() < quantity) {
                throw new ServiceException("Sin stock suficiente. Disponible: " + article.getAmount() + " - Requerido: " + quantity);

            }
            int newAmount = article.getAmount() - quantity;
            articleDAO.updateAmount(articleId, newAmount);
        } catch (SQLException e) {
            throw new ServiceException("Error al retirar stock: " + e.getMessage());
        }
    }

        public void remove(int articleId) throws ServiceException{
        requireAdmin("eliminar artículo");
        try{
            articleDAO.findById(articleId).orElseThrow(()-> new ServiceException(
                    "No se encontró el artículo con ID " + articleId
            ));

            boolean deleted = articleDAO.delete(articleId);
            if (!deleted) {
                throw  new ServiceException("No se pudo eliminar el artículo. Puede estar en una venta en proceso.");
            }

        }catch (SQLException e){
            throw new ServiceException("Error al eliminar el artículo: " + e.getMessage());
        }
    }
}



























