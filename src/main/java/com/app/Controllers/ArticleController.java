package com.app.Controllers;


import Infrastructure.security.SessionManager;
import com.app.Model.Enum.ArticleCategory;
import com.app.Model.Enum.ItemState;
import com.app.Model.Enum.SourceType;
import com.app.Model.domain.Article;
import com.app.Service.ArticleService;
import com.app.Service.exceptions.ServiceException;

import javax.swing.*;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.List;


public class ArticleController extends BaseController {
    private ArticleService articleService;

    public ArticleController() {
        this.articleService = new ArticleService();
    }

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    public void loadAll(Component parent, OnSuccess<List<Article>> onSuccess, OnError onError) {
        log.info("Cargando inventario completo de artículos");
        runAsync(
                () -> {
                    try {
                        return articleService.getAll();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                result -> {
                    log.debug("Inventario cargado: {} artículo(s)", result.size());
                    onSuccess.onResult(result);
                },
                (msg, ex) -> {
                    log.error("Error al cargar inventario: {}", msg);
                    onError.onError("Error al cargar el inventario: " + msg, ex);
                }
        );
    }

    public void loadAvailableArticles(Component parent, OnSuccess<List<Article>> onSuccess, OnError onError) {
        log.debug("Cargar articulos disponibles para la venta o empeño");
        runAsync(
                () -> {
                    try {
                        return articleService.getAvailableForSaleOrPawn();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                }, onSuccess,
                (msg, ex) -> onError.onError("Error al cargar articulos disponibles: " + msg, ex));
    }

    /*
     * Busca artículos cuyo nombre contenga el término dado.
     * @param term -> término de búsqueda (no puede ser nulo ni vacío)
     * @param parent-> componente padre para mensajes de error
     * @param onSuccess callback con los resultados (en EDT)
     * @param onError callback con el mensaje de error (en EDT)
     */
    public void searchArticles(String term, Component parent, OnSuccess<List<Article>> onSuccess, OnError onError) {
        if (term == null || term.isBlank()) {
            loadAll(parent, onSuccess, onError);
            return;
        }
        log.debug("Buscar articulos disponibles con termino : '{}'", term);
        runAsync(
                () -> {
                    try {
                        return articleService.search(term.trim());
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                }, onSuccess,
                (msg, ex) -> onError.onError("Error el la Busqueda:" + msg, ex)
        );
    }

    /* -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------
     * Crea un nuevo artículo en el inventario.
     * Solo disponible para administradores.
     * @param article   artículo a crear (sin ID)
     * @param parent    componente padre para mensajes
     * @param onSuccess callback invocado si se creó correctamente (en EDT)
     * @param onError   callback con el mensaje de error (en EDT)
     */
    public void createArticle(Article article, Component parent, OnSuccess<Article> onSuccess, OnError onError) {
        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede crear un articulos");
            return;
        }
        log.info("Creando articulo: {}", article.getNameArticle());
        runAsync(
                () -> {
                    try {
                        return articleService.create(article);
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                result -> {
                    log.info("Articlo creadi con el ID:{}", result.getId());
                    showSuccess(parent, "Articulo creado con el ID: " + result.getId() +
                            "creado con le nombre de: {" + result.getNameArticle() + "}");
                    onSuccess.onResult(result);
                },
                (msg, ex) -> {
                    log.error("Error al crear articulo: {}", msg);
                    onError.onError("Error al crear articulo: " + msg, ex);
                }
        );
    }

    /*-------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------
     * Actualiza todos los campos de un artículo existente.
     * Solo disponible para administradores.
     * @param article   artículo con datos actualizados (debe tener ID)
     * @param parent    componente padre para mensaje
     */
    public void editArticle(Article article, Component parent, Runnable onSuccess, OnError onError) {
        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede editar articulos.");
            return;
        }
        log.info("Editando articulo: {} a {}", article.getId());
        runAsyncVoid(
                () -> articleService.edit(article),
                () -> {
                    log.info("Articulo con ID: {} actulizado", article.getId());
                    showSuccess(parent, "Articulo Actulizado correctamente " + article.getId());
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al editar articulo: {}", article.getId(), msg);
                    onError.onError("Error al editar articulo: " + msg, ex);
                }
        );
    }

    /*
     * Actualiza los campos básicos de un artículo (nombre, descripción, categoría).
     * @param id          ID del artículo a actualizar
     * @param nameArticle nuevo nombre
     * @param description nueva descripción
     * @param category    nueva categoría
     * @param sourceType  nuevo tipo de fuente (puede ser null)
     * @param itemState   nuevo estado del ítem (puede ser null)
     */
    public void editBasicFields(
            int id,
            String nameArticle,
            String description,
            ArticleCategory category,
            SourceType sourceType,
            ItemState itemState,
            Component parent,
            Runnable onSuccess,
            OnError onError) {
        log.info("Editando articulo ID = {}", id);
        runAsyncVoid(
                () -> articleService.editBasicFields(id, nameArticle, description, category, sourceType, itemState),
                () -> {
                    showSuccess(parent, "Articulo Actualizado correctamente " + id);
                    onSuccess.run();
                },
                (msg, ex) -> onError.onError("Error al editar articulo: " + msg, ex)
        );
    }

    /*
     * Actualiza el precio de un artículo.
     * Solo disponible para administradores (RF-03.5).
     * @param articleId ID del artículo
     * @param newPrice  nuevo precio (debe ser mayor a 0)
     */
    public void editPrice(int articleId, BigDecimal newPrice, Component parent, Runnable onSuccess, OnError onError) {
        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede editar precios $");
            return;
        }
        log.info("Actulizar precio del articulo: ${} a ${}", articleId, newPrice);
        runAsyncVoid(
                () -> articleService.editPrice(articleId, newPrice),
                () -> {
                    showSuccess(parent, "Articulo Actualizado correctamente $: " + newPrice);
                    onSuccess.run();
                },
                (msg, ex) -> onError.onError("Error al editar precio del articulo: " + msg, ex)
        );
    }

    /*
     * Agrega unidades al stock de un artículo.
     * Disponible para Admin y Empleado (RF-03.6).
     *
     * @param articleId ID del artículo
     * @param quantity  cantidad a agregar (debe ser mayor a 0)
     */
    public void addStock(int articleId, int quantity, Component parent, Runnable onSuccess, OnError onError) {
        log.info("Agregando {} unidades del articulo nombre: {}", quantity, articleId);
        runAsyncVoid(
                () -> articleService.addStock(articleId, quantity),
                () -> {
                    showSuccess(parent, quantity + " Unidad(es)  Agregado correctamente al inventario");
                    onSuccess.run();
                },
                (msg, ex) -> onError.onError("Errror al agregar al stock: " + msg, ex)
        );
    }

    /*
     * Retira unidades del stock de un artículo.
     * @param articleId ID del artículo
     * @param quantity  cantidad a retirar (debe ser mayor a 0 y ≤ stock actual)
     */
    public void removeStock(int articleId, int quantity, Component parent, Runnable onSuccess, OnError onError) {
        log.info("Retirando {} unidades del articulo ID: {}", quantity, articleId);
        runAsyncVoid(
                () -> articleService.removeStock(articleId, quantity),
                () -> {
                    showSuccess(parent, quantity + " unidad(es) retirada(s) del inventario.");
                    onSuccess.run();
                },
                (msg, ex) -> onError.onError("Error al retirar stock: " + msg, ex)
        );
    }

    public void delete(int articleId, String articleName, Component parent, Runnable onSuccess, OnError onError) {
        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede eliminar artículos.");
            return;
        }
        boolean confirmed = showConfirmation(
                parent,
                "¿Eliminar el artículo \"" + articleName + "\"?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación");

        if (!confirmed) return;

        log.info("Eliminando artículo ID={} ('{}')", articleId, articleName);
        runAsyncVoid(
                () -> articleService.remove(articleId),
                () -> {
                    log.info("Artículo ID={} eliminado", articleId);
                    showSuccess(parent, "Artículo eliminado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al eliminar artículo ID={}: {}", articleId, msg);
                    onError.onError("No se pudo eliminar el artículo: " + msg, ex);
                }
        );
    }

    // -------------------------------------------------------
    // Permisos (helpers para las vistas)
    // -------------------------------------------------------

    public boolean isAdmin() {
        return SessionManager.isAdmin();
    }
}
