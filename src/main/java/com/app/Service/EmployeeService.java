package com.app.Service;

import com.app.Service.exceptions.ServiceException;
import com.app.Model.Dao.EmployeeDao;
import com.app.Model.domain.Employee;

import java.sql.SQLException;
import java.util.List;

public class EmployeeService {
    private final EmployeeDao employeeDAO = new EmployeeDao();

    public Employee findById(String id) throws ServiceException {
        try {
            return employeeDAO.findById(id)
                    .orElseThrow(() -> new ServiceException("No se encontró el empleado con ID: " + id));
        } catch (SQLException e) {
            throw new ServiceException("Error al buscar empleado: " + e.getMessage());
        }
    }


    public List<Employee> findAll() throws ServiceException {
        try {
            return employeeDAO.findAll();
        } catch (SQLException e) {
            throw new ServiceException("Error al listar los empleados");
        }
    }

    public void setActive(String id, boolean active) throws ServiceException {
        try {
            employeeDAO.updateActive(id, active);

        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar el estado: " + e.getMessage());

        }
    }

    public void create(Employee employee) throws ServiceException {
        try {
            employeeDAO.save(employee);
        } catch (SQLException e) {
            throw new ServiceException("Error al crear el empleado: " + e.getMessage());
        }
    }

    public void update(Employee employee) throws ServiceException {
        try {
            boolean updated = employeeDAO.update(employee);
            if (!updated) {
                throw new ServiceException("No se pudo actualizar el empleado. Posiblemente no existe.");
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al actualizar el empleado: " + e.getMessage());
        }
    }

    public void delete(String id) throws ServiceException {
        try {
            boolean deleted = employeeDAO.delete(id);
            if (!deleted) {
                throw new ServiceException("No se pudo eliminar el empleado.");
            }
        } catch (SQLException e) {
            throw new ServiceException("Error al eliminar el empleado: " + e.getMessage());
        }
    }
}
