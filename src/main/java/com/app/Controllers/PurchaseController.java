package com.app.Controllers;

import com.app.Model.domain.Article;
import com.app.Model.domain.Cliente;
import com.app.Model.domain.Purchase;
import com.app.Service.PurchaseService;

import javax.swing.*;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.List;

public class PurchaseController extends BaseController {

    private final PurchaseService purchaseService;

    public PurchaseController() {
        this.purchaseService = new PurchaseService();
    }

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    public void loadAll(OnSuccess<List<Purchase>> onSuccess, OnError onError) {
        log.info("Cargando historial de compras");
        runAsync(
                () -> {
                    try {
                        return purchaseService.getAll();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> {
                    log.error("Error al cargar compras: {}", msg);
                    onError.onError("Error al cargar compras: " + msg, ex);
                }
        );
    }

    /**
     * Registra una nueva compra (transacción atómica: cliente? + artículo + purchase).
     *
     * @param article       artículo a crear en inventario
     * @param purchasePrice precio pagado al proveedor
     * @param clienteId     ID de cliente existente (0 si es nuevo)
     * @param clienteRapido cliente nuevo a crear (null si es existente)
     * @param notes         observaciones opcionales
     * @param parent        componente padre para mensajes
     * @param onSuccess     callback si se registró correctamente
     * @param onError       callback de error
     */
    public void register(Article article, BigDecimal purchasePrice, int clienteId,
                         Cliente clienteRapido, String notes,
                         Component parent, Runnable onSuccess, OnError onError) {
        log.info("Registrando nueva compra directa");
        runAsyncVoid(
                () -> purchaseService.register(article, purchasePrice, clienteId, clienteRapido, notes),
                () -> {
                    log.info("Compra registrada exitosamente");
                    showSuccess(parent, "Compra registrada correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al registrar compra: {}", msg);
                    onError.onError("Error al registrar compra: " + msg, ex);
                }
        );
    }
}
