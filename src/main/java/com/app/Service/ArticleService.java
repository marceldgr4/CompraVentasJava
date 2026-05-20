package com.app.Service;

import com.app.Infrastructure.security.SessionManager;
import com.app.Model.Enum.ItemState;
import com.app.Model.Enum.SourceType;
import com.app.Model.domain.Article;
import com.app.Model.Enum.ArticleCategory;
import com.app.Repositories.ArticleRepository;
import com.app.Repositories.impl.ArticleRespositoryImpl;
import com.app.Service.exceptions.BusinessException;
import com.app.Service.exceptions.ServiceException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ArticleService {

    //private final ArticleDao articleDao = new ArticleDao();
    private final ArticleRepository articleRepository;

    public ArticleService() {
        this.articleRepository = new ArticleRespositoryImpl();
    }

    // -------------------------------------------------------
    // READ — lista completa del inventario
    // -------------------------------------------------------
    public List<Article> getAll() throws ServiceException {
        try {
            return articleRepository.findAll();

        } catch (Exception e) {
            throw new ServiceException("Error al cargar el inventario: " + e.getMessage(),e);
        }
    }

  /* public List<Article> getAvailableForSaleOrPawn() throws ServiceException {
        try{
            return articleDao.findAvailable();

        }catch (SQLException e){
            throw new ServiceException("Error al cargar el inventario disponible: " + e.getMessage(),e);
        }
   }*/

    // -------------------------------------------------------
    // READ — buscar por nombre
    // -------------------------------------------------------
    public List<Article> search(String name) throws ServiceException {
        if (name == null || name.isBlank()) {
            throw new ServiceException("El nombre no puede ser nulo o estar vacío");
        }
        try {
            return articleRepository.findByName(name.trim());
        } catch (Exception e) {
            throw new ServiceException("Error en la búsqueda...: " + e.getMessage(),e);
        }
    }

    // -------------------------------------------------------
    // READ — todos con stock disponible (para panel de empleado)
    // -------------------------------------------------------
    public List<Article> getAvailableForSaleOrPawn() throws ServiceException {
        try {
            return articleRepository.findAll()
                    .stream()
                    .filter(Article::hasStock)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServiceException("Error al cargar el inventario del empleado: " + e.getMessage(),e);
        }
    }
    public Article getById(int id) throws ServiceException {
        try{
            return articleRepository.findById(id).orElseThrow(()-> new ServiceException("Articulo no encotrado con ID:"+id));
        }catch (Exception e){
            throw new ServiceException("Error al buscar articulo:"+ e.getMessage(),e);
        }
    }

    // -------------------------------------------------------
    // CREATE — solo Admin puede crear artículos
    // -------------------------------------------------------

    public Article create(Article article) throws ServiceException {
        validateArticle(article);
        try {
            return articleRepository.save(article);
        } catch (Exception e) {
            throw new ServiceException("Error al crear el artículo: " + e.getMessage(),e);
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
    /*
     * Edita nombre, descripción y categoría. Disponible para Admin y Empleado (RF-03.5).
     */
    // -------------------------------------------------------
    public void editBasicFields(int id, String nameArticle, String description, ArticleCategory category, SourceType sourceType, ItemState itemState) throws ServiceException {
        if(nameArticle == null || nameArticle.isBlank()) {
            throw new BusinessException("El  nombre del articulo es obligatorio ");
        }
        if(category == null) {
            throw new BusinessException("la categoria es obligatoria");
        }
        try{
            boolean update = articleRepository.update(id,nameArticle.trim(),description,category,sourceType,itemState);
            if(!update) {
                throw new ServiceException("Articulo no encontrado con ID:"+id);
            }
        } catch (Exception e) {
            throw new ServiceException("Error al actualizar el articulo: " + e.getMessage(),e);
        }
    }

    /*
     * Edita el precio. Solo Admin puede hacer esto directamente).
     * El Empleado debe usar el flujo de autorización temporal).
     */
    public void editPrice (int id, BigDecimal newPrice) throws ServiceException {
        requireAdmin("editar precios del articulo");
        if(newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La precio debe ser mayor que $0.");
        }
        try{
            articleRepository.findById(id).orElseThrow(()-> new ServiceException("Articulo no encontrado con ID:"+id));
            boolean update = articleRepository.updatePrice(id,newPrice);
            if(!update) {
                throw new ServiceException("No se pudo actulziar el precio del Articulo no encontrado con ID:"+id);
            }
        }catch (Exception e){
            throw new ServiceException("Error al actualizar el articulo: " + e.getMessage(),e);
        }
    }

    public void edit(Article article) throws ServiceException {
        requireAdmin("editar artículo");
        validateArticle(article);
        try{
            boolean updated = articleRepository.update(article);
            if (!updated) {
                throw new ServiceException("Error: no se encontró el artículo con ID " + article.getId());
            }
        } catch (Exception e){
            throw new ServiceException("Error al actualizar el artículo: " + e.getMessage(),e);
        }
    }
        //---------
        // UPDATE == Agrega unidades al stock. Admin y Empleado (RF-03.6).
        //--------
    public void addStock(int articleId, int quantity) throws ServiceException {
        if (quantity <= 0) {
            throw new ServiceException("La cantidad debe ser mayor que 0.");
        }
        try{
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(()-> new ServiceException(
                            "No se encontró el artículo con ID " + articleId
                    ));
            int newAmount = article.getAmount() + quantity;
            articleRepository.updateAmount(articleId, newAmount);
        } catch (Exception e){
            throw new ServiceException("Error al actualizar el artículo: " + e.getMessage(),e);
        }

    }
    public void removeStock(int articleId, int quantity) throws ServiceException {
        if (quantity <= 0) {
            throw new BusinessException("La cantidad debe ser mayor que 0.");

        }
        try {
            Article article = getById(articleId);
            if(article.getAmount() < quantity) {
                throw new BusinessException("Stock insufucuente para el articulo:'" + article.getNameArticle()+
                        "'. disponible: " + article.getAmount()+ "requerido: " + quantity);
            }
            articleRepository.updateAmount(articleId, article.getAmount() - quantity);
        }
        catch (Exception e){
            throw new ServiceException("Error al retirar stock: " + e.getMessage(),e);
        }
    }

        public void remove(int articleId) throws ServiceException{
        requireAdmin("eliminar artículo");
        try{
            articleRepository.findById(articleId).orElseThrow(()-> new ServiceException(
                    "No se encontró el artículo con ID " + articleId));

            boolean deleted = articleRepository.delete(articleId);
            if (!deleted) {
                throw  new ServiceException("No se pudo eliminar el artículo. Puede estar en una venta en proceso.");
            }

        }catch (Exception e){
            throw new ServiceException("Error al eliminar el artículo: " + e.getMessage(),e);
        }
    }
}



























