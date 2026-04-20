package com.app.Repositories.impl;

import com.app.Model.Dao.PawnDao;
import com.app.Model.domain.Pawn;
import com.app.Repositories.PawnRespository;

import java.util.List;
import java.util.Optional;

public class PawnRespositoryImpl implements PawnRespository {
    private final PawnDao pawnDao = new PawnDao();

    @Override
    public List<Pawn> findAll() throws Exception {
        return List.of();
    }

    @Override
    public Optional<Pawn> findById(int id) throws Exception {
        return Optional.empty();
    }

    @Override
    public List<Pawn> findByCliente(String first_name) throws Exception {
        return List.of();
    }

    @Override
    public List<Pawn> findBylast_name(String last_name) throws Exception {
        return List.of();
    }

    @Override
    public List<Pawn> findActive() throws Exception {
        return List.of();
    }

    @Override
    public List<Pawn> findOverdue() throws Exception {
        return List.of();
    }

    @Override
    public Pawn save(Pawn pawn) throws Exception {
        return null;
    }

    @Override
    public boolean update(Pawn pawn) throws Exception {
        return false;
    }

    @Override
    public boolean markAsReturned(int id) throws Exception {
        return false;
    }

    @Override
    public boolean markAsExpired(int id) throws Exception {
        return false;
    }

    @Override
    public int expireOverduePawns() throws Exception {
        return 0;
    }

    @Override
    public boolean delete(int id) throws Exception {
        return false;
    }
}
