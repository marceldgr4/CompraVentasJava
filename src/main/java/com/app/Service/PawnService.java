package com.app.Service;

import com.app.Dao.PawnDao;
import com.app.Model.Pawn;
import com.app.Model.SesionUser;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Locale.filter;

public class PawnService {
    private final PawnDao pawnDao = new PawnDao();

    public List<Pawn> getAll()  throws SQLException {
        SesionUser session = SesionUser.getInstance();

        if(session.isAdmin() || session.isEmployee()){
            return pawnDao.findAll();
        }else {
            return pawnDao.findByProfile(session.getProfileId());
        }
    }
    /*
     * Busca un empeño por ID con validación de permisos.
     * @throws SecurityException si un empleado intenta acceder a empeño ajeno
     */
    public Optional<Pawn> getById(int id) throws SQLException {
        Optional<Pawn> pawnOptional = pawnDao.findById(id);

        if(pawnOptional.isPresent()){
            Pawn pawn = pawnOptional.get();
            SesionUser session = SesionUser.getInstance();
            if(!session.isAdmin() && !pawn.getProfile_id().equals(session.getProfileId())){
                throw new SecurityException("You are not allowed to access this pawn");
            }
        }
        return pawnOptional;
    }
    /*
     * Obtiene solo articullo empeños activos (no devueltos ni expirados).
     */
    public  List<Pawn>getActivePawns() throws SQLException {
        List<Pawn> allActivePawns = pawnDao.findActive();
        SesionUser session = SesionUser.getInstance();
        if(session.isAdmin() || session.isEmployee()){
            return allActivePawns;
        }else {
            return allActivePawns.stream()
            .filter(p-> p.getProfile_id().equals(session.getProfileId()))
                    .toList();
        }
    }
/*
 * Obtiene empeños vencidos (pasaron fecha límite).
 */
    public List<Pawn> getOverduePawns() throws SQLException {
        List<Pawn> allOverduePawns = pawnDao.findOverdue();
        SesionUser session = SesionUser.getInstance();
        if(session.isAdmin() || session.isEmployee()){
            return allOverduePawns;
        }else {
            return allOverduePawns.stream()
                .filter(p-> p.getProfile_id().equals(session.getProfileId()))
                .toList();

        }
    }
    /*
     * Crea un nuevo artículo de empeño con validaciones de negocio.
     * @throws IllegalArgumentException si las validaciones fallan
     */
    public Pawn create(Pawn pawn) throws SQLException {
        validatePawn(pawn);
        SesionUser session = SesionUser.getInstance();

        if (pawn.getPawn_date() == null) {
            pawn.setPawn_date(LocalDate.now());
        }
        return pawnDao.save(pawn);
    }

    /*
     * Actualizar artículo de empeño existente con validación de permisos.
     *
     * @throws SecurityException si empleado intenta modificar empeño ajeno
     */
    public Pawn update(Pawn pawn) throws SQLException {
        validatePawn(pawn);
        // verificar permsisos
        Optional<Pawn> existing = pawnDao.findById(pawn.getId());
        if (existing.isPresent()) {
            SesionUser session = SesionUser.getInstance();
            if (!session.isAdmin() && !existing.get().getProfile_id().equals(session.getProfileId())) {
                throw new SecurityException("You are not allowed to access this pawn");
            }
        }
        boolean updated = pawnDao.update(pawn);
        if (!updated) {
            throw new SQLException("Pawn could not be updated");
        }
        return pawn;
    }

        /*
         * Marca un artículo empeñodo como devuelto.
         ** @throws SecurityException si no tiene permisos
         */
     public void markAsReturned (int id) throws SQLException {
         Optional<Pawn> pawnOptional = pawnDao.findById(id);
         if (pawnOptional.isEmpty()) {
             throw new SQLException("Pawn could not be found");
         }
         Pawn pawn = pawnOptional.get();
         if (pawn.isReturned()) {
             throw new IllegalArgumentException("the article Pawn has already been marked as returned");
         }
         boolean updated = pawnDao.markAsReturned(id);
         if (!updated) {
             throw new SQLException("the article Pawn could not be marked as returned");
         }
     }

         /*
          * Marca un empeño como expirado (solo Admin).
          * @throws SecurityException si no es Admin
          */
         public void markAsExpired(int id) throws SQLException{
             if (!SesionUser.getInstance().isAdmin()) {
                 throw new SecurityException("Only the admin can mark a pawned item as expired.");
             }
             boolean updated = pawnDao.markAsExpired(id);
             if (!updated) {
                 throw new SQLException("not can mark article expired");
         }
    }

    /*
     Procesa automáticamente los articulos empeñodos que esta vencidos (solo Admin).
     * Marca como expirados todos los que pasaron su fecha límite.
     * @return Cantidad de empeños marcados como expirados
     */
    public int processOverduePawns() throws SQLException {
        if(!SesionUser.getInstance().isAdmin()){
            throw new SecurityException("You are not allowed to access this pawn");
        }
        return pawnDao.expireOverduePawns();
    }

    /*
     * Elimina un articulo empeñado (solo Admin).
     * @throws SecurityException si no es Admin
     */
     public void delete(int id) throws SQLException {
        if (!SesionUser.getInstance().isAdmin()) {
            throw new SecurityException("Only the admin can mark a artcile pawned item as deleted.");
        }
        boolean deleted = pawnDao.delete(id);
        if (!deleted) {
            throw new SQLException("Pawn could not be deleted");
        }
     }

     public BigDecimal getTotalActiveValues() throws SQLException {
        List<Pawn> allActivePawns = getActivePawns();
        return  allActivePawns.stream()
                .map(Pawn::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
     }

    /*
     * Validar las reglas de negocio de los articulos de empeño.
     * @throws IllegalArgumentException si alguna validación falla
     */
private void validatePawn(Pawn pawn)  {
    check(pawn.getArticle_id() <= 0, "Debe seleccionar un artículo válido");
    check(pawn.getCliente_id() <= 0, "Debe seleccionar un cliente válido");
    check(pawn.getAmount() <= 0, "La cantidad debe ser mayor a 0");
    check(pawn.getPrice() == null || pawn.getPrice().compareTo(BigDecimal.ZERO) <= 0, "El precio debe ser mayor a 0");
    check(pawn.getPawn_date() == null, "La fecha de empeño es obligatoria");
    check(pawn.getReturn_date() == null, "La fecha de devolución es obligatoria");

    if (pawn.getPawn_date() != null && pawn.getReturn_date() != null && !pawn.getReturn_date().isAfter(pawn.getPawn_date())) {
        throw new IllegalArgumentException("La fecha de devolución debe ser posterior a la fecha de empeño");
    }
}

    private void check(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
