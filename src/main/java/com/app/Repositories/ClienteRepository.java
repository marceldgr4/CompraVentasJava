package com.app.Repositories;

import com.app.Model.domain.Cliente;


import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    Optional<Cliente> findById(int id) throws Exception;
    List<Cliente> findAll() throws Exception;
    List<Cliente> findByFirstName(String first_name) throws Exception;
    List<Cliente> findByLastName(String last_name) throws Exception;

    boolean deleteById(int id) throws Exception;
    Cliente save(Cliente cliente) throws Exception;
    boolean updateActive(boolean active) throws Exception;

}
