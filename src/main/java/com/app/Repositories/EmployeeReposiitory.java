package com.app.Repositories;


import com.app.Model.domain.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeReposiitory {
    Optional<Employee> findByFullName(String fullName) throws Exception;
    List<Employee> findAll() throws Exception;
    boolean updateActive( boolean active) throws Exception;
}
