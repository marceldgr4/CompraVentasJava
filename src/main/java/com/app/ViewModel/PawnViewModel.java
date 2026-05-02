package com.app.ViewModel;

import com.app.Model.Enum.PawnStatus;
import com.app.Model.domain.Pawn;
import com.app.Service.PawnService;
import com.app.Service.exceptions.ServiceException;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PawnViewModel extends BaseViewModel {
    private final PawnService pawnService = new PawnService();
    private List<Pawn> pawns = new ArrayList<>();
    private List<Pawn> filteredPawns = new ArrayList<>();

    public void LoadAllPawns() throws ServiceException {
        pawns = pawnService.getAll();
        filteredPawns = new ArrayList<>();
        notifyObservers("Pawns_Loaded", pawns);
    }

    public void loadActivePawns() throws ServiceException {
        filteredPawns = pawnService.getActivePawns();
        notifyObservers("Active_Pawns_Loaded", filteredPawns);
    }

    public void loadOverPawns() throws ServiceException {
        filteredPawns = pawnService.getOverduePawns();
        notifyObservers("Overdue_Pawns_Loaded", filteredPawns);
    }

    public void createNewPawn(Pawn pawn) throws ServiceException {
        Pawn created = pawnService.create(pawn);
        pawns.add(created);
        filteredPawns.add(created);
        notifyObservers("New_Pawn_Created", created);
    }

    public void updatePawn(Pawn pawn) throws ServiceException {
        pawnService.update(pawn);
        pawns.replaceAll(p -> p.getId() == pawn.getId() ? pawn : p);
        filteredPawns.replaceAll(p -> p.getId() == pawn.getId() ? pawn : p);
        notifyObservers("Pawn_Updated", pawn);
    }

    public void markAsResturned(int pawnId) throws ServiceException {
        pawnService.markAsReturned(pawnId);
        pawns.stream().filter(p-> p.getId() == pawnId).findFirst().
                ifPresent(p -> p.setStatus(PawnStatus.Retirado));
        notifyObservers("Pawn_Mark_As_Returned", pawns);
    }

    public void deletePawn(int pawnId) throws ServiceException {
        pawnService.delete(pawnId);
        pawns.removeIf(p -> p.getId() == pawnId);
        notifyObservers("Pawn_Deleted", pawns);
    }

    public List<Pawn> getPawns() {
        return new ArrayList<>(pawns);
    }

    public List<Pawn> getFilteredPawns() {
        return new ArrayList<>(filteredPawns);
    }

    public BigDecimal getTotalActiveValue() throws ServiceException {
        return pawnService.getTotalActiveValues();
    }
}
