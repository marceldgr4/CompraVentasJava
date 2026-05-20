package com.app.Service;

import com.app.Infrastructure.DataBase.DataBaseManeger;
import com.app.Infrastructure.security.SessionManager;
import com.app.Model.Dao.ArticleDao;
import com.app.Model.Dao.PawnDao;
import com.app.Model.domain.Article;
import com.app.Model.domain.Pawn;
import com.app.Model.Enum.PawnStatus;
import com.app.Service.exceptions.BusinessException;
import com.app.Service.exceptions.ServiceException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para Empeños (Pawns).
 * Aplica control de acceso basado en el rol del usuario activo.
 */
public class PawnService {

    private final PawnDao pawnDao = new PawnDao();
    private final ArticleDao articleDao = new ArticleDao();

    /*-------------------------------------------------------
    // READ — listar empeños según rol del usuario
     * Retorna todos los empeños.
     * Admin/Empleado ven todos; en caso de rol futuro restringido, filtra por empleado.
     */

    // -------------------------------------------------------
    public List<Pawn> getAll() throws ServiceException {
        try {
            return pawnDao.findAll();
        }catch (SQLException ex) {
            throw new ServiceException("Error la cargar articulo empeñado" + ex.getMessage());
        }
    }

    public Optional<Pawn> getById(int id) throws ServiceException {
        try {
            return pawnDao.findById(id);

        } catch (SQLException e) {
            throw new ServiceException("Error al buscar empeño id=" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Retorna empeños activos (no devueltos ni expirados).
     */
    public List<Pawn> getActivePawns() throws ServiceException {
        try {
           return pawnDao.findActive();
        } catch (SQLException e) {
            throw new ServiceException("Error al cargar empeños activos: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna empeños vencidos (pasaron fecha límite y no fueron devueltos).
     */
    public List<Pawn> getOverduePawns() throws ServiceException {
        try {
            return pawnDao.findOverdue();
        } catch (SQLException e) {
            throw new ServiceException("Error al cargar empeños vencidos: " + e.getMessage(), e);
        }
    }
public List<Pawn> getStatus(PawnStatus status) throws ServiceException {
        try{
            return pawnDao.findByStatus(status);
        }catch (SQLException e){
            throw new ServiceException("Error al filtar articulos empañdo"+ e.getMessage(), e);
        }
}
    // -------------------------------------------------------
    /* ── CREATE — transacción atómica con reducción de stock
     * Registra un nuevo empeño.
     * La reducción de stock es atómica: si falla el INSERT del empeño,
     * el UPDATE del artículo también hace rollback y viceversa.
     * Validación de peso para Joyería (RF-04.13).
     */
    // -------------------------------------------------------

    public Pawn create(Pawn pawn) throws ServiceException {
        validatePawn(pawn);
        try {
            Article article = articleDao.findById(pawn.getArticleId()).orElseThrow(()-> new ServiceException(
                    "Articlos no encotrado ID:"+ pawn.getArticleId()
            ));
            if (article.getAmount()< pawn.getAmount()) {
                throw new BusinessException("Stock insuficientes para le articulos selecionado."+ "cantidad Disponible"+ article.getAmount());
            }
            // Validación de peso para Joyería
            if(article.requireWeigthForPawn() && (pawn.getWeightGrams()==null ||
                    pawn.getWeightGrams().compareTo(BigDecimal.ZERO)<0)) {
                throw new BusinessException(
                        "el peso en gramos es obligatorio para el articulo de joyeria");

            }
            final  int newAmount = article.getAmount() - pawn.getAmount();
            // Transacción atómica: INSERT pawn + UPDATE article.amount
            DataBaseManeger.runInTransaction(con -> {
                pawnDao.save(con, pawn);
                articleDao.updateAmountTransactional(con,
                        article.getId(),
                        newAmount);
            });

        }catch (SQLException e){
            throw new ServiceException("Error al realizar el registro de Empeño" + e.getMessage(), e);
        }
        return pawn;
    }

    public Pawn registerAgilePawn(Pawn pawn, Article article, com.app.Model.domain.Cliente clienteRapido) throws ServiceException {
        validatePawn(pawn);
        try {
            final int[] resolvedClienteId = {pawn.getClientId()};
            final int[] resolvedArticleId = {pawn.getArticleId()};

            DataBaseManeger.runInTransaction(con -> {
                // 1. Guardar cliente rápido si existe
                if (clienteRapido != null) {
                    clienteRapido.setRegistrationType(com.app.Model.Enum.RegistrationType.RAPIDO);
                    com.app.Model.domain.Cliente saved = new com.app.Model.Dao.ClienteDao().save(con, clienteRapido);
                    resolvedClienteId[0] = saved.getId();
                }

                // 2. Guardar artículo nuevo si existe
                if (article != null) {
                    article.setClienteId(resolvedClienteId[0]);
                    article.setSourceType(com.app.Model.Enum.SourceType.EMPENO);
                    article.setAmount(pawn.getAmount()); // Inicialmente tiene la cantidad a empeñar
                    com.app.Model.domain.Article savedArticle = new com.app.Model.Dao.ArticleDao().save(con, article);
                    resolvedArticleId[0] = savedArticle.getId();
                }

                pawn.setClientId(resolvedClienteId[0]);
                pawn.setArticleId(resolvedArticleId[0]);

                // 3. Ejecutar la lógica de creación de empeño y reducción de stock
                com.app.Model.domain.Article art = articleDao.findById(resolvedArticleId[0]).orElseThrow(() -> new SQLException(
                        "Artículo no encontrado ID: " + resolvedArticleId[0]
                ));
                if (art.getAmount() < pawn.getAmount()) {
                    throw new BusinessException("Stock insuficiente para el artículo seleccionado.");
                }
                if (art.requireWeigthForPawn() && (pawn.getWeightGrams() == null ||
                        pawn.getWeightGrams().compareTo(BigDecimal.ZERO) < 0)) {
                    throw new BusinessException("El peso en gramos es obligatorio para el artículo de joyería.");
                }

                final int newAmount = art.getAmount() - pawn.getAmount();
                pawnDao.save(con, pawn);
                articleDao.updateAmountTransactional(con, art.getId(), newAmount);
            });

            return pawn;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new ServiceException("Ya existe un cliente con ese número de cédula o teléfono.");
            }
            throw new ServiceException("Error al registrar empeño ágil: " + e.getMessage(), e);
        }
    }
    /*-------------------------------------------------------
    // UPDATE
    * Marca un empeño como Retirado (cliente devuelve el artículo).
    * Disponible para Empleado (proepios) y Admin (todos) .
    */
    public void update(Pawn pawn) throws ServiceException {
        requireAdmin("Editar articulo empeñado");
        try{
            pawnDao.updateStatus(pawn.getId(),pawn.getStatus());
        }catch (SQLException e){
            throw new ServiceException("Error al actuluza el articulo empeñado" + e.getMessage(), e);
        }
    }


    public void markAsReturned(int id ) throws ServiceException{
        try{
            Pawn pawn = pawnDao.findById(id).
                    orElseThrow(()-> new ServiceException("Empeño no encontrado id" + id));
        if(!SessionManager.isAdmin() && !pawn.getEmployeeId().equals(SessionManager.getEmployeeId())) {
            throw new ServiceException("Solo puede marcar como devuelto sus propios empeños ");
        }
        if(pawn.getStatus()!= null && pawn.getStatus().isTerminal()) {
            throw new BusinessException("El articulo empeñado ya esta en estado '" + pawn.getStatusLabel()+"' y no se puede modificar.");
        }
        pawnDao.markAsReturned(id);
        }catch (SQLException e){
            throw new ServiceException("Error al marcar el articulo empeñado devuelto: " + e.getMessage(), e);
        }
    }

    public void markAsExpired(int id) throws ServiceException {
        requireAdmin("marcar articulo empeñado como vencido manualmente");
        try{
            pawnDao.markAsExpired(id);
        }catch (SQLException e){
            throw  new ServiceException("Error al Marcar articulo empañado vencido: " + e.getMessage());
        }

    }
    /*
     * Procesa automáticamente todos los empeños vencidos llamando a
     * {@code fn_expire_overdue_pawns()} en la BD.
     * se ejecuta automáticamente al iniciar la app desde un SwingWorker,
     * independiente del rol del usuario. Solo la expiración MANUAL requiere Admin.
     */

    public int processOverduePawns() throws ServiceException {
        try {
            return pawnDao.expireOverduePawns();
        } catch (SQLException e) {
            throw new ServiceException("Error al procesar empeños vencidos: " + e.getMessage(), e);
        }
    }
    /*
     * Marca un empeño como Perdido.
     * Se activa cuando {@code installments_missed > 4} (RF-04.12).
     * Solo Admin puede ejecutarlo manualmente.
     */
    public void markAsPerdido( int id ) throws ServiceException{
        requireAdmin("marcar empeño como perdido");
        try {
            Pawn pawn = pawnDao.findById(id).orElseThrow(() -> new ServiceException("Empeño no encontrado id =" + id));
            if (pawn.getStatus() == PawnStatus.Activo || pawn.getStatus() == PawnStatus.Vencido) {
                pawnDao.updateStatus(id, PawnStatus.Perdido);

            } else {
                throw new BusinessException("Solo se puede marcar como perdido el articulo empeñado activo o vencidios.");
            }
        }
        catch(SQLException e){
                throw new ServiceException("Error al marcar empeño perdido:" + e.getMessage(), e);
            }
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------

    /**
     * Elimina un empeño. Solo el Admin puede ejecutar esta acción.
     */
    public void delete(int id) throws ServiceException {
        requireAdmin("eliminar articulo empeño");
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


    private void validatePawn(Pawn pawn) {
        check(pawn.getEmployeeId()== null || pawn.getEmployeeId().isBlank(),"El ID del empleado es obligatorio");
        check(pawn.getArticleId() <= 0,   "Debe seleccionar un artículo válido.");
        check(pawn.getClientId() <= 0,    "Debe seleccionar un cliente válido.");
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
