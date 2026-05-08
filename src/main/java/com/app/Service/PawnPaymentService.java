package com.app.Service;

import Infrastructure.security.SessionManager;
import com.app.Model.Dao.PawnDao;
import com.app.Model.Dao.PawnPaymentDao;
import com.app.Model.domain.Pawn;
import com.app.Model.domain.PawnPayment;
import com.app.Service.exceptions.BusinessException;
import com.app.Service.exceptions.ServiceException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PawnPaymentService {
    private final PawnPaymentDao paymentDao = new PawnPaymentDao();
    private final PawnDao pawnDao = new PawnDao();

    public PawnPayment registerPayment(int pawnId, BigDecimal amount, String notes) throws ServiceException {
        if(amount==null || amount.compareTo(BigDecimal.ZERO)<=0) {
            throw  new BusinessException(" El monto del pago debe ser mayor a $ 0.00");
        }
        try{
            Pawn pawn = pawnDao.findById(pawnId).orElseThrow(()-> new ServiceException("El articulo empeñado id="+ pawnId + "No encottado"));
            if(!pawn.acceptsPayment()){
                throw new BusinessException("El articulo empeñado esta en estado de_'"+ pawn.getStatusLabel()+ "' y no se acepta pagos");
            }
            PawnPayment payment = new PawnPayment(
            pawnId, amount, null, notes, SessionManager.getEmployeeId(), false);
            return paymentDao.save(payment);
        }catch (SQLException e){
            throw  new ServiceException("Error al registar el pago: " +e.getMessage(),e);
        }
    }
    public PawnPayment registerMissedInstallment(int pawnId, String notes) throws ServiceException {
       requireAdmin("registar coutas vencidas");
        try{
            Pawn pawn = pawnDao.findById(pawnId).orElseThrow(()-> new ServiceException("El articulo empeñado con Id:'"+pawnId+ "'No encotrado"));
            if(!pawn.acceptsPayment()){
                throw new BusinessException("El articulo tiene el estado: '" + pawn.getStatusLabel() + "' y no se acepta pagos vencidas");

            }
            PawnPayment missed = PawnPayment.missedInstallment(
                    pawnId,SessionManager.getEmployeeId(),notes);
            return paymentDao.save(missed);
        }catch (SQLException e){
            throw  new ServiceException("Error al registar el pago: " +e.getMessage(),e);
        }
    }
    private void requireAdmin(String action) throws ServiceException {
        if(!SessionManager.isAdmin()){
            throw new ServiceException("Solo el administrador puede " + action+ ".");
        }
    }
    public List<PawnPayment> getPaymentsByPawn(int pawnId) throws ServiceException {
        try{
            return paymentDao.findByPawnId(pawnId);
        }catch (SQLException e){
            throw new ServiceException("Error al cargar pago del articulo: " +e.getMessage(),e);
        }
    }
}
