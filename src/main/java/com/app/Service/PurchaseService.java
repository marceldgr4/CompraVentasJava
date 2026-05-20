package com.app.Service;

import com.app.Infrastructure.DataBase.DataBaseManeger;
import com.app.Infrastructure.security.SessionManager;
import com.app.Model.Dao.ArticleDao;
import com.app.Model.Dao.ClienteDao;
import com.app.Model.Dao.PurchaseDao;
import com.app.Model.Enum.RegistrationType;
import com.app.Model.Enum.SourceType;
import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Model.domain.Purchase;
import com.app.Service.exceptions.BusinessException;
import com.app.Service.exceptions.ServiceException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PurchaseService {

    private final PurchaseDao purchaseDao = new PurchaseDao();
    private final ArticleDao articleDao = new ArticleDao();
    private final ClienteDao clienteDao = new ClienteDao();

    public List<Purchase> getAll() throws ServiceException {
        try {
            if (SessionManager.isAdmin()) return purchaseDao.findAll();
            return purchaseDao.findByEmployee(SessionManager.getEmployeeId());

        } catch (SQLException e) {
            throw new ServiceException("Error al cargar compras: " + e.getMessage(), e);
        }
    }

    public List<Purchase> findByDateRange(LocalDate from, LocalDate to) throws ServiceException {
        if (from == null || to == null || from.isAfter(to)) {
            throw new BusinessException("Rango de fecha invalido");
        }
        try {
            return purchaseDao.findByDateRange(from, to);
        } catch (SQLException e) {
            throw new ServiceException("Error al filtrar compras: " + e.getMessage(), e);
        }
    }

    /*
    //  CREATE — transacción atómica: cliente? + article + purchase
     * Registra una compra del negocio a un cliente/proveedor.
         * @param artículo a crear en inventario (sin ID, sin clienteId)
         * @param purchasePrice precio que el negocio pagó al proveedor
         * @param clienteId ID de cliente existente (0 si es cliente nuevo)
         * @param clienteRapido cliente nuevo a crear (null si es existente)
         * @param notes observaciones opcionales
         * @return Purchase persistida con ID asignado
     */

    public Purchase register(Article article, BigDecimal purchasePrice, int clienteId, Cliente clienteRapido, String notes) throws ServiceException {
        validate(article, purchasePrice);
        try {
            final int[] resolvedClienteId = {clienteId};
            final int[] resolvedArticleId = {0};
            Purchase purchase = new Purchase(
                    SessionManager.getEmployeeId(), 0, 0, purchasePrice, notes);


            DataBaseManeger.runInTransaction(con -> {
                if (clienteRapido != null) {
                    clienteRapido.setRegistrationType(RegistrationType.RAPIDO);
                    Cliente saved = clienteDao.save(con, clienteRapido);
                    resolvedClienteId[0] = saved.getId();
                }

                article.setClienteId(resolvedClienteId[0]);
                article.setSourceType(SourceType.COMPRA);
                article.setPurchasePrice(purchasePrice);
                article.setAmount(1);

                Article savedArticle = articleDao.save(con, article);
                resolvedArticleId[0] = savedArticle.getId();

                purchase.setClientId(resolvedClienteId[0]);
                purchase.setArticleId(resolvedArticleId[0]);
                purchaseDao.save(con, purchase);
            });
            return purchase;

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ServiceException("Ya existe un cliente con ese numero de cedula o telefono");
            }
            throw new ServiceException("Error al registrar compra: " + e.getMessage(), e);
        }


    }

    //--------------
    private void validate(Article article, BigDecimal purchasePrice) {
        if (article == null || article.getNameArticle() == null || article.getNameArticle().isBlank()) {
            throw new BusinessException("El nombre del artículo es obligatorio.");
        }
        if (article.getCategory() == null) {
            throw new BusinessException("La categoría del artículo es obligatoria.");
        }
        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio de compra debe ser mayor a $0.");
        }
        if (article.getPrice() == null || article.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio de venta sugerido debe ser mayor a $0.");
        }
    }
}
