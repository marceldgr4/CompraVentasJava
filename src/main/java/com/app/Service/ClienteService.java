package com.app.Service;

import Infrastructure.security.SessionManager;
import com.app.Model.Dao.ClienteDao;
import com.app.Model.domain.Cliente;
import com.app.Model.Enum.ClienteStatus;
import com.app.Service.exceptions.BusinessException;
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
            return SessionManager.isAdmin() ?
                    clienteDao.findAll()
                    : clienteDao.getAllActive();
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
        ClienteStatus filter = SessionManager.isAdmin() ? null : ClienteStatus.Activo;
        try {
            return clienteDao.findByTerm(term.trim(), filter);
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
            String msg = e.getMessage();
            if (isUniqueViolation(e)){
                throw new ServiceException("ya existe un cliente con el mismo Correo.");
            }
            throw new ServiceException("Error al crear el cliente: " + msg, e);
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

    /* -------------------------------------------------------
     * DELETE — solo Admin
     * Cambia el status del cliente a Eliminado sin borrar el registro.
     * Disponible para Empleado y Admin.
     */
    // -------------------------------------------------------

    public void softDelete(int id) throws ServiceException {
        //requireAdmin("eliminar cliente");
        try {
            findById(id);
            boolean updated = clienteDao.softDelete(id);
            if (!updated) {
                throw new ServiceException("No se puede marcar el Cliente como eliminado." );
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar el cliente: " + e.getMessage(), e);
        }
    }
    /*
     * Eliminación física. Solo Admin.
     * Falla si el cliente tiene ventas, empeños o artículos asociados (ON DELETE RESTRICT).
     */
    public void hardDelete(int id) throws ServiceException {
        requireAdmin("eliminar cliente de forma permanente");
    try {
        findById(id);
        boolean deleted = clienteDao.delete(id);
        if (!deleted) {
            throw new ServiceException("No se puede marcar el Cliente con ID:."+ id);
        }
    }catch (SQLException e) {
        if(isFKViolation(e)){
          throw new ServiceException("Error al eliminar el cliente tiene operacion regsitados ");
        }
        throw new ServiceException("Error al eliminar el cliente: " + e.getMessage(), e);
        }
    }
    private boolean isFKViolation(SQLException e) {
        return "23503".equals(e.getSQLState());
    }
    private boolean isUniqueViolation(SQLException e) {
        return "23505".equals(e.getSQLState());
    }

    // -------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------

    private void requireAdmin(String action) throws ServiceException {
        if (!SessionManager.isAdmin()) {
            throw new ServiceException("Solo el administrador puede: " + action);
        }
    }

    private void validate(Cliente cliente)  {
        List<String> errors = new ArrayList<>();
        checkField(cliente.getFirstName(), "El nombre es obligatorio.", errors);
        checkField(cliente.getLastName(),  "El apellido es obligatorio.", errors);
        checkField(cliente.getEmail(),     "El correo es obligatorio.", errors);
        checkField(cliente.getPhone(),     "El teléfono es obligatorio.", errors);
        if (!errors.isEmpty()) {
            throw new BusinessException(String.join(" ", errors));
        }

    }

    private void checkField(String value, String errorMsg, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(errorMsg);
        }
    }
}
