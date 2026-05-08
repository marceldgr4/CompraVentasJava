package com.app.Controllers;

import com.app.Model.domain.Sale;
import com.app.Service.SaleService;
import com.app.Service.exceptions.ServiceException;

import javax.swing.*;
import java.awt.Component;
import java.time.LocalDate;
import java.util.List;

public class SaleController extends BaseController {

    private final SaleService saleService;

    public SaleController() {
        this.saleService = new SaleService();
    }

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    public void loadAll(OnSuccess<List<Sale>> onSuccess, OnError onError) {
        log.info("Cargando todas las ventas");
        runAsync(
                () -> {
                    try {
                        return (List<Sale>) saleService.getAllSales();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> {
                    log.error("Error al cargar ventas: {}", msg);
                    onError.onError("Error al cargar ventas: " + msg, ex);
                }
        );
    }

    public void filter(LocalDate from, LocalDate to, int clienteId, OnSuccess<List<Sale>> onSuccess, OnError onError) {
        log.info("Filtrando ventas: from={}, to={}, clienteId={}", from, to, clienteId);
        runAsync(
                () -> {
                    if (clienteId > 0) {
                        try {
                            return saleService.findByCliente(clienteId);
                        } catch (ServiceException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (from != null && to != null) {
                        try {
                            return saleService.findByDateRange(from, to);
                        } catch (ServiceException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        return (List<Sale>) saleService.getAllSales();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> {
                    log.error("Error al filtrar ventas: {}", msg);
                    onError.onError("Error al filtrar ventas: " + msg, ex);
                }
        );
    }

    public void create(Sale sale, Component parent, Runnable onSuccess, OnError onError) {
        log.info("Registrando nueva venta");
        runAsyncVoid(
                () -> saleService.create(sale),
                () -> {
                    log.info("Venta registrada exitosamente");
                    showSuccess(parent, "Venta registrada correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al registrar venta: {}", msg);
                    onError.onError("Error al registrar venta: " + msg, ex);
                }
        );
    }

    public void delete(int id, Component parent, Runnable onSuccess, OnError onError) {
        boolean confirmed = showConfirmation(parent, 
                "¿Eliminar la venta #" + id + "?\nEsta acción no se puede deshacer.", 
                "Confirmar eliminación");
        
        if (!confirmed) return;

        log.info("Eliminando venta ID: {}", id);
        runAsyncVoid(
                () -> saleService.delete(id),
                () -> {
                    log.info("Venta eliminada exitosamente");
                    showSuccess(parent, "Venta eliminada correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al eliminar venta: {}", msg);
                    onError.onError("Error al eliminar venta: " + msg, ex);
                }
        );
    }
}
