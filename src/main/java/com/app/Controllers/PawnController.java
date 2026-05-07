package com.app.Controllers;

import com.app.Model.domain.Pawn;
import com.app.Service.PawnService;
import com.app.Service.exceptions.ServiceException;

import javax.swing.*;
import java.awt.Component;
import java.util.List;
import java.util.Optional;

public class PawnController extends BaseController {

    private final PawnService pawnService;

    public PawnController() {
        this.pawnService = new PawnService();
    }

    public PawnController(PawnService pawnService) {
        this.pawnService = pawnService;
    }

    public void loadAll(OnSuccess<List<Pawn>> onSuccess, OnError onError) {
        log.info("Cargando todos los empeños");
        runAsync(
                () -> {
                    try {
                        return pawnService.getAll();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> {
                    log.error("Error al cargar empeños: {}", msg);
                    onError.onError("Error al cargar empeños: " + msg, ex);
                }
        );
    }

    public void filter(String status, OnSuccess<List<Pawn>> onSuccess, OnError onError) {
        log.info("Filtrando empeños por estado: {}", status);
        runAsync(
                () -> {
                    try {
                        return switch (status) {
                            case "Activos" -> pawnService.getActivePawns();
                            case "Vencidos" -> pawnService.getOverduePawns();
                            default -> pawnService.getAll();
                        };
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> {
                    log.error("Error al filtrar empeños: {}", msg);
                    onError.onError("Error al filtrar empeños: " + msg, ex);
                }
        );
    }

    public void create(Pawn pawn, Component parent, Runnable onSuccess, OnError onError) {
        log.info("Creando nuevo empeño");
        runAsyncVoid(
                () -> pawnService.create(pawn),
                () -> {
                    log.info("Empeño creado exitosamente");
                    showSuccess(parent, "Empeño creado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al crear empeño: {}", msg);
                    onError.onError("Error al crear empeño: " + msg, ex);
                }
        );
    }

    public void update(Pawn pawn, Component parent, Runnable onSuccess, OnError onError) {
        log.info("Actualizando empeño ID: {}", pawn.getId());
        runAsyncVoid(
                () -> pawnService.update(pawn),
                () -> {
                    log.info("Empeño actualizado exitosamente");
                    showSuccess(parent, "Empeño actualizado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al actualizar empeño: {}", msg);
                    onError.onError("Error al actualizar empeño: " + msg, ex);
                }
        );
    }

    public void markAsReturned(int id, Component parent, Runnable onSuccess, OnError onError) {
        log.info("Marcando empeño ID: {} como devuelto", id);
        runAsyncVoid(
                () -> pawnService.markAsReturned(id),
                () -> {
                    log.info("Empeño marcado como devuelto");
                    showSuccess(parent, "Empeño marcado como devuelto correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al marcar empeño como devuelto: {}", msg);
                    onError.onError("Error al procesar devolución: " + msg, ex);
                }
        );
    }

    public void delete(int id, Component parent, Runnable onSuccess, OnError onError) {
        boolean confirmed = showConfirmation(parent,
                "¿Eliminar el empeño #" + id + "?",
                "Confirmar eliminación");

        if (!confirmed) return;

        log.info("Eliminando empeño ID: {}", id);
        runAsyncVoid(
                () -> pawnService.delete(id),
                () -> {
                    log.info("Empeño eliminado exitosamente");
                    showSuccess(parent, "Empeño eliminado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al eliminar empeño: {}", msg);
                    onError.onError("Error al eliminar empeño: " + msg, ex);
                }
        );
    }

    public void processOverdue(Component parent, OnSuccess<Integer> onSuccess, OnError onError) {
        log.info("Procesando empeños vencidos");
        runAsync(
                () -> {
                    try {
                        return pawnService.processOverduePawns();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> {
                    log.error("Error al procesar vencidos: {}", msg);
                    onError.onError("Error al procesar vencidos: " + msg, ex);
                }
        );
    }

    public void getTotalActiveValue(OnSuccess<String> onSuccess, OnError onError) {
        runAsync(
                () -> {
                    try {
                        return pawnService.getTotalActiveValues().toString();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> onError.onError("Error al obtener total: " + msg, ex)
        );
    }

    public void getById(int id, OnSuccess<Optional<Pawn>> onSuccess, OnError onError) {
        runAsync(
                () -> {
                    try {
                        return pawnService.getById(id);
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                onError
        );
    }
}
