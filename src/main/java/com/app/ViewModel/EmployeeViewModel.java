package com.app.ViewModel;

import com.app.Model.domain.Employee;
import com.app.Service.EmployeeService;
import com.app.Service.exceptions.ServiceException;

import java.util.ArrayList;
import java.util.List;

public class EmployeeViewModel extends BaseViewModel {
    private final EmployeeService employeeService = new EmployeeService();
    private List<Employee> employees = new ArrayList<>();

    public void loadAllEmployees() throws ServiceException {
        employees = employeeService.findAll();
        notifyObservers("Employee_loaded", employees);
    }

    public void setEmployeesActive(String employeeId, boolean active) throws ServiceException {
        employeeService.setActive(employeeId, active);
        employees.replaceAll(employee -> employee.getId().equals(employeeId) ? new Employee(employeeId, employee.getFullName(), employee.getRol(), active) : employee);
        notifyObservers("Employee_Updated", employeeId);
    }

    public List<Employee> getEmployees() {
        return new ArrayList<>(employees);
    }
}
