package com.app.Repositories;

import com.app.Model.domain.Pawn;

import java.util.List;
import java.util.Optional;

public interface PawnRespository {
    List<Pawn> findAll() throws  Exception;
    Optional<Pawn> findById(int id) throws Exception;
    List<Pawn> findByCliente(String first_name) throws Exception;
    List<Pawn> findBylast_name(String last_name) throws Exception;
    List<Pawn> findActive() throws Exception;
    List<Pawn> findOverdue() throws Exception;

    Pawn save(Pawn pawn) throws Exception;
    boolean update(Pawn pawn) throws Exception;
    boolean markAsReturned(int id) throws Exception;
    boolean markAsExpired(int id) throws Exception;
    int expireOverduePawns() throws Exception;
    boolean delete(int id) throws Exception;
}
