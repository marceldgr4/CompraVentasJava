package com.app.Service;

import Infrastructure.security.SessionManager;
import com.app.Model.Dao.PawnDao;
import com.app.Model.domain.Pawn;
import com.app.Service.exceptions.BusinessException;
import com.app.Service.exceptions.ServiceException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para Empeños (Pawns).
 * Aplica control de acceso basado en el rol del usuario activo.
 */
public class PawnService {

    private final PawnDao pawnDao = new PawnDao();

    // -------------------------------------------------------
    // READ — listar empeños según rol del usuario
    // -------------------------------------------------------

    /**
     * Retorna todos los empeños.
     * Admin/Empleado ven todos; en caso de rol futuro restringido, filtra por profile.
     */
    public List<Pawn> getAll() throws ServiceException {
        try {
            if (SessionManager.isAdmin() || SessionManager.isEmployee()) {
                return pawnDao.findAll();
            }
            // Rol sin acceso global: solo sus propios empeños
            return pawnDao.findByProfile(SessionManager.getProfileId());
        } catch (SQLException e) {
            throw new ServiceException("Error al cargar empeños: " + e.getMessage(), e);
        }
    }

    /**
     * Busca un empeño por ID, validando permisos de acceso.
     *
     * @throws SecurityException si un empleado intenta acceder a empeño ajeno
     */
    public Optional<Pawn> getById(int id) throws ServiceException {
        try {
            Optional<Pawn> pawnOpt = pawnDao.findById(id);
            pawnOpt.ifPresent(pawn -> {
                if (!SessionManager.isAdmin()
                        && !pawn.getProfileId().equals(SessionManager.getProfileId())) {
                    throw new SecurityException("No tiene permiso para acceder a este empeño.");
                }
            });
            return pawnOpt;
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar empeño id=" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Retorna empeños activos (no devueltos ni expirados).
     */
    public List<Pawn> getActivePawns() throws ServiceException {
        try {
            List<Pawn> all = pawnDao.findActive();
            if (SessionManager.isAdmin() || SessionManager.isEmployee()) {
                return all;
            }
            return all.stream()
                    .filter(p -> p.getProfileId().equals(SessionManager.getProfileId()))
                    .toList();
        } catch (SQLException e) {
            throw new ServiceException("Error al cargar empeños activos: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna empeños vencidos (pasaron fecha límite y no fueron devueltos).
     */
    public List<Pawn> getOverduePawns() throws ServiceException {
        try {
            List<Pawn> all = pawnDao.findOverdue();
            if (SessionManager.isAdmin() || SessionManager.isEmployee()) {
                return all;
            }
            return all.stream()
                    .filter(p -> p.getProfileId().equals(SessionManager.getProfileId()))
                    .toList();
        } catch (SQLException e) {
            throw new ServiceException("Error al cargar empeños vencidos: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    /**
     * Crea un nuevo empeño con validación de reglas de negocio.
     */
    public Pawn create(Pawn pawn) throws ServiceException {
        validatePawn(pawn);
        if (pawn.getPawnDate() == null) {
            pawn.setPawnDate(LocalDate.now());
        }
        try {
            return pawnDao.save(pawn);
        } catch (SQLException e) {
            throw new ServiceException("Error al crear el empeño: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    /**
     * Actualiza un empeño existente con validación de permisos.
     *
     * @throws SecurityException si un empleado intenta modificar empeño ajeno
     */
    public Pawn update(Pawn pawn) throws ServiceException {
        validatePawn(pawn);
        try {
            Optional<Pawn> existing = pawnDao.findById(pawn.getId());
            if (existing.isPresent()
                    && !SessionManager.isAdmin()
                    && !existing.get().getProfileId().equals(SessionManager.getProfileId())) {
                throw new SecurityException("No tiene permiso para modificar este empeño.");
            }
            boolean updated = pawnDao.update(pawn);
            if (!updated) {
                throw new ServiceException("No se pudo actualizar el empeño id=" + pawn.getId());
            }
            return pawn;
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar el empeño: " + e.getMessage(), e);
        }
    }

    /**
     * Marca un empeño como devuelto.
     */
    public void markAsReturned(int id) throws ServiceException {
        try {
            Pawn pawn = pawnDao.findById(id)
                    .orElseThrow(() -> new ServiceException("Empeño no encontrado id=" + id));
            if (pawn.isReturned()) {
                throw new BusinessException("El empeño ya fue marcado como devuelto.");
            }
            boolean updated = pawnDao.markAsReturned(id);
            if (!updated) {
                throw new ServiceException("No se pudo marcar el empeño como devuelto.");
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al marcar empeño devuelto: " + e.getMessage(), e);
        }
    }

    /**
     * Marca un empeño como expirado. Solo el Admin puede ejecutar esta acción.
     */
    public void markAsExpired(int id) throws ServiceException {
        requireAdmin("marcar empeño como expirado");
        try {
            boolean updated = pawnDao.markAsExpired(id);
            if (!updated) {
                throw new ServiceException("No se pudo marcar el empeño como expirado id=" + id);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al marcar empeño expirado: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa automáticamente todos los empeños vencidos marcándolos como expirados.
     * Solo el Admin puede ejecutar esta acción.
     *
     * @return Número de empeños actualizados
     */
    public int processOverduePawns() throws ServiceException {
        requireAdmin("procesar empeños vencidos");
        try {
            return pawnDao.expireOverduePawns();
        } catch (SQLException e) {
            throw new ServiceException("Error al procesar empeños vencidos: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------

    /**
     * Elimina un empeño. Solo el Admin puede ejecutar esta acción.
     */
    public void delete(int id) throws ServiceException {
        requireAdmin("eliminar empeño");
        try {
            boolean deleted = pawnDao.delete(id);
            if (!deleted) {
                throw new ServiceException("No se pudo eliminar el empeño id=" + id);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar el empeño: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // CÁLCULOS
    // -------------------------------------------------------

    /**
     * Calcula el valor total de todos los empeños activos.
     */
    public BigDecimal getTotalActiveValues() throws ServiceException {
        return getActivePawns().stream()
                .map(Pawn::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // -------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------

    private void requireAdmin(String action) throws ServiceException {
        if (!SessionManager.isAdmin()) {
            throw new ServiceException("Solo el administrador puede: " + action);
        }
    }

    /**
     * Valida las reglas de negocio de un empeño.
     *
     * @throws BusinessException si alguna validación falla
     */
    private void validatePawn(Pawn pawn) {
        check(pawn.getArticleId() <= 0,   "Debe seleccionar un artículo válido.");
        check(pawn.getClienteId() <= 0,    "Debe seleccionar un cliente válido.");
        check(pawn.getAmount()    <= 0,    "La cantidad debe ser mayor a 0.");
        check(pawn.getPrice() == null || pawn.getPrice().compareTo(BigDecimal.ZERO) <= 0,
                "El precio debe ser mayor a 0.");
        check(pawn.getPawnDate()   == null, "La fecha de empeño es obligatoria.");
        check(pawn.getReturnDate() == null, "La fecha de devolución es obligatoria.");

        if (pawn.getPawnDate() != null && pawn.getReturnDate() != null
                && !pawn.getReturnDate().isAfter(pawn.getPawnDate())) {
            throw new BusinessException(
                    "La fecha de devolución debe ser posterior a la fecha de empeño.");
        }
    }

    private void check(boolean condition, String message) {
        if (condition) throw new BusinessException(message);
    }
}
