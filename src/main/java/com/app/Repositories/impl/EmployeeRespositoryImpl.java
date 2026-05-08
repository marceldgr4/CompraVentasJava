package com.app.Repositories.impl;

import com.app.Model.Dao.EmployeeDao;
import com.app.Model.domain.Employee;
import com.app.Repositories.EmployeeReposiitory;

import java.util.List;
import java.util.Optional;

public class EmployeeRespositoryImpl implements EmployeeReposiitory {
    private final EmployeeDao employeeDao = new EmployeeDao();

    @Override
    public Optional<Employee> findByFullName(String fullName) throws Exception {
        return Optional.empty();
    }

    @Override
    public List<Employee> findAll() throws Exception {
        return List.of();
    }

    @Override
    public boolean updateActive(boolean active) throws Exception {
        return false;
    }
}
