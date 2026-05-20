package com.app.Service;

import com.app.Infrastructure.security.SessionManager;
import com.app.Model.Dao.ClienteDao;
import com.app.Model.Enum.RegistrationType;
import com.app.Model.domain.Cliente;
import com.app.Model.Enum.ClienteStatus;
import com.app.Service.exceptions.BusinessException;
import com.app.Service.exceptions.ServiceException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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

    /**
     * Busca un cliente por teléfono exacto (para detectar duplicados en registro rápido).
     */
    public Optional<Cliente> findByPhone(String phone) throws ServiceException {
        if (phone == null || phone.isBlank()) return Optional.empty();
        try {
            return clienteDao.findByPhone(phone.trim());
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar por teléfono: " + e.getMessage(), e);
        }
    }

    public Optional<Cliente> findByCedula(String cedula) throws ServiceException {
        if (cedula == null || cedula.isBlank()) return Optional.empty();
        try {
            return clienteDao.findByCedula(cedula.trim());
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar por cedula: " + e.getMessage(), e);

        }
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    public Cliente create(Cliente cliente) throws ServiceException {
        validateCompleto(cliente);
        return persist(cliente);
    }

    public Cliente createRapido(String firstName, String cedula, String phone) throws ServiceException {
        if (firstName == null || firstName.isBlank()) {
            throw new BusinessException("El nombre completo es Obligatorio para el regsitro rapida");
        }
        if (cedula != null && !cedula.isBlank()) {
            Optional<Cliente> existing = findByCedula(cedula.trim());
            if (existing.isPresent()) {
                throw new BusinessException(
                        "Ya existe un cliente con la cédula " + cedula +
                                " (ID: " + existing.get().getId() + "). Usa 'Seleccionar existente'.");
            }
        }

        if (phone != null && !phone.isBlank()) {
            Optional<Cliente> existing = findByPhone(phone.trim());
            if (existing.isPresent()) {
                throw new BusinessException(
                        "Ya existe un cliente con el teléfono " + phone +
                                " (ID: " + existing.get().getId() + "). Usa 'Seleccionar existente'.");
            }
        }

        Cliente rapido = Cliente.createRapido(
                cedula != null ? cedula.trim() : null,
                firstName.trim(),
                null,
                phone != null ? phone.trim() : null
        );
        return persist(rapido);
    }


    // -------------------------------------------------------
    // UPDATE — solo Admin
    // -------------------------------------------------------

    public void update(Cliente cliente) throws ServiceException {
        requireAdmin("editar cliente");
        // Si se agregan apellido + email a un cliente RAPIDO, se promueve a COMPLETO
        if (cliente.getRegistrationType() == RegistrationType.RAPIDO
                && cliente.getLastName() != null && !cliente.getLastName().isBlank()
                && cliente.getEmail() != null && !cliente.getEmail().isBlank()) {
            cliente.setRegistrationType(RegistrationType.COMPLETO);
        }
        // Para COMPLETO, validar campos completos
        if (cliente.getRegistrationType() == RegistrationType.COMPLETO) {
            validateCompleto(cliente);
        }
        try {
            boolean updated = clienteDao.update(cliente);
            if (!updated) throw new ServiceException("Cliente no encontrado con id: " + cliente.getId());
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
                throw new ServiceException("No se puede marcar el Cliente como eliminado.");
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
                throw new ServiceException("No se puede marcar el Cliente con ID:." + id);
            }
        } catch (SQLException e) {
            if (isFKViolation(e)) {
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
    private Cliente persist(Cliente cliente) throws ServiceException {
        try {
            return clienteDao.save(cliente);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                String msg = e.getMessage().contains("phone")
                        ? "Ya existe un cliente con ese teléfono."
                        : e.getMessage().contains("email")
                          ? "Ya existe un cliente con ese correo."
                          : "Ya existe un cliente con esos datos.";
                throw new ServiceException(msg);
            }
            throw new ServiceException("Error al guardar el cliente: " + e.getMessage(), e);
        }
    }

    private void requireAdmin(String action) throws ServiceException {
        if (!SessionManager.isAdmin()) {
            throw new ServiceException("Solo el administrador puede: " + action);
        }
    }

    private void validateCompleto(Cliente cliente) {
        List<String> errors = new ArrayList<>();

        checkField(cliente.getFirstName(), "El nombre es obligatorio.", errors);
        checkField(cliente.getLastName(), "El apellido es obligatorio.", errors);

        // Cédula: si se provee, debe ser numérica (validación de formato)
        if (cliente.getCedula() != null && !cliente.getCedula().isBlank()) {
            if (!cliente.getCedula().trim().matches("^[0-9]+$")) {
                errors.add("La cédula solo acepta números.");
            }
        }

        // Teléfono: si se provee, debe tener formato válido (alineado con CHECK en BD)
        if (cliente.getPhone() != null && !cliente.getPhone().isBlank()) {
            String cleanPhone = cliente.getPhone().trim().replaceAll("[^0-9+]", "");
            if (!cleanPhone.matches("^[+]?[0-9]{7,15}$")) {
                errors.add("El teléfono debe tener entre 7 y 15 dígitos.");
            }
        }

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