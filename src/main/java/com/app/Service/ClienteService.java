package com.app.Service;

import Infrastructure.security.SessionManager;
import com.app.Model.Dao.ClienteDao;
import com.app.Model.domain.Cliente;
import com.app.Service.exceptions.ServiceException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de lógica de negocio para Clientes.
 */
public class ClienteService {

    private final ClienteDao clienteDao = new ClienteDao();

    // -------------------------------------------------------
    // READ
    // -------------------------------------------------------

    public List<Cliente> getAll() throws ServiceException {
        try {
            return clienteDao.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Error al cargar clientes: " + e.getMessage(), e);
        }
    }

    public Cliente findById(int id) throws ServiceException {
        try {
            return clienteDao.findById(id)
                    .orElseThrow(() -> new ServiceException("Cliente no encontrado con id: " + id));
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar cliente: " + e.getMessage(), e);
        }
    }

    public List<Cliente> search(String term) throws ServiceException {
        if (term == null || term.isBlank()) {
            throw new ServiceException("El término de búsqueda no puede estar vacío.");
        }
        try {
            return clienteDao.findByTerm(term.trim());
        } catch (SQLException e) {
            throw new ServiceException("Error en búsqueda de clientes: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    public Cliente create(Cliente cliente) throws ServiceException {
        validate(cliente);
        try {
            return clienteDao.save(cliente);
        } catch (SQLException e) {
            throw new ServiceException("Error al crear el cliente: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // UPDATE — solo Admin
    // -------------------------------------------------------

    public void update(Cliente cliente) throws ServiceException {
        requireAdmin("editar cliente");
        validate(cliente);
        try {
            boolean updated = clienteDao.update(cliente);
            if (!updated) {
                throw new ServiceException("Cliente no encontrado con id: " + cliente.getId());
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar el cliente: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // DELETE — solo Admin
    // -------------------------------------------------------

    public void delete(int id) throws ServiceException {
        requireAdmin("eliminar cliente");
        try {
            boolean deleted = clienteDao.delete(id);
            if (!deleted) {
                throw new ServiceException("Cliente no encontrado con id: " + id);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar el cliente: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------

    private void requireAdmin(String action) throws ServiceException {
        if (!SessionManager.isAdmin()) {
            throw new ServiceException("Solo el administrador puede: " + action);
        }
    }

    private void validate(Cliente cliente) throws ServiceException {
        List<String> errors = new ArrayList<>();
        checkField(cliente.getFirstName(), "El nombre es obligatorio.", errors);
        checkField(cliente.getLastName(),  "El apellido es obligatorio.", errors);
        checkField(cliente.getEmail(),     "El correo es obligatorio.", errors);
        checkField(cliente.getPhone(),     "El teléfono es obligatorio.", errors);
        if (!errors.isEmpty()) {
            throw new ServiceException(String.join(", ", errors));
        }
    }

    private void checkField(String value, String errorMsg, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(errorMsg);
        }
    }
}
