package com.app.Controllers;

import com.app.Model.domain.Cliente;
import com.app.Service.ClienteService;
import com.app.Service.exceptions.ServiceException;

import javax.swing.*;
import java.awt.Component;
import java.util.List;

public class ClienteController extends BaseController {

    private final ClienteService clienteService;

    public ClienteController() {
        this.clienteService = new ClienteService();
    }

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public void loadAll(Component parent, OnSuccess<List<Cliente>> onSuccess, OnError onError) {
        log.info("Cargando todos los clientes");
        runAsync(
                () -> {
                    try {
                        return clienteService.getAll();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> {
                    log.error("Error al cargar clientes: {}", msg);
                    onError.onError("Error al cargar clientes: " + msg, ex);
                }
        );
    }

    public void search(String term, Component parent, OnSuccess<List<Cliente>> onSuccess, OnError onError) {
        if (term == null || term.isBlank()) {
            loadAll(parent, onSuccess, onError);
            return;
        }
        log.info("Buscando clientes con término: {}", term);
        runAsync(
                () -> {
                    try {
                        return clienteService.search(term.trim());
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> {
                    log.error("Error en búsqueda de clientes: {}", msg);
                    onError.onError("Error en búsqueda: " + msg, ex);
                }
        );
    }

    public void create(Cliente cliente, Component parent, Runnable onSuccess, OnError onError) {
        log.info("Creando nuevo cliente: {} {}", cliente.getFirstName(), cliente.getLastName());
        runAsyncVoid(
                () -> clienteService.create(cliente),
                () -> {
                    log.info("Cliente creado exitosamente");
                    showSuccess(parent, "Cliente creado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al crear cliente: {}", msg);
                    onError.onError("Error al crear cliente: " + msg, ex);
                }
        );
    }

    public void update(Cliente cliente, Component parent, Runnable onSuccess, OnError onError) {
        log.info("Actualizando cliente ID: {}", cliente.getId());
        runAsyncVoid(
                () -> clienteService.update(cliente),
                () -> {
                    log.info("Cliente actualizado exitosamente");
                    showSuccess(parent, "Cliente actualizado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al actualizar cliente: {}", msg);
                    onError.onError("Error al actualizar cliente: " + msg, ex);
                }
        );
    }

    public void delete(int id, String name, Component parent, Runnable onSuccess, OnError onError) {
        boolean confirmed = showConfirmation(parent,
                "¿Eliminar al cliente \"" + name + "\"?\nEsta acción es irreversible.",
                "Confirmar eliminación");

        if (!confirmed) return;

        log.info("Eliminando cliente ID: {}", id);
        runAsyncVoid(
                () -> clienteService.softDelete(id),
                () -> {
                    log.info("Cliente eliminado exitosamente");
                    showSuccess(parent, "Cliente eliminado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al eliminar cliente: {}", msg);
                    onError.onError("Error al eliminar cliente: " + msg, ex);
                }
        );
    }

    public void getById(int id, OnSuccess<Cliente> onSuccess, OnError onError) {
        runAsync(
                () -> {
                    try {
                        return clienteService.findById(id);
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                onError
        );
    }
}
