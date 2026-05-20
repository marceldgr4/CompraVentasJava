package com.app.Controllers;

import com.app.Infrastructure.security.SessionManager;
import com.app.Model.domain.Employee;
import com.app.Service.EmployeeService;
import com.app.Service.AuthService;
import com.app.Service.exceptions.ServiceException;

import java.awt.Component;
import java.util.List;

public class EmployeeController extends BaseController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    public EmployeeController() {
        this.employeeService = new EmployeeService();
    }

    // -------------------------------------------------------
    // READ
    // -------------------------------------------------------

    public void loadAll(Component parent, OnSuccess<List<Employee>> onSuccess, OnError onError) {
        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede ver los empleados.");
            return;
        }
        log.info("Cargando todos los empleados");
        runAsync(
                () -> {
                    try {
                        return employeeService.findAll();
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                result -> {
                    log.debug("Empleados cargados: {}", result.size());
                    onSuccess.onResult(result);
                },
                (msg, ex) -> {
                    log.error("Error al cargar empleados: {}", msg);
                    onError.onError("Error al cargar empleados: " + msg, ex);
                }
        );
    }

    public void loadById(String employeeId, Component parent,
                         OnSuccess<Employee> onSuccess, OnError onError) {
        if (employeeId == null || employeeId.isBlank()) {
            onError.onError("El ID del empleado no puede ser nulo.", null);
            return;
        }
        log.debug("Buscando empleado con ID: {}", employeeId);
        runAsync(
                () -> {
                    try {
                        return employeeService.findById(employeeId);
                    } catch (ServiceException e) {
                        throw new RuntimeException(e);
                    }
                },
                onSuccess,
                (msg, ex) -> onError.onError("Error al buscar empleado: " + msg, ex)
        );
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    public void setActive(String employeeId, boolean active, String userName,
                          Component parent, Runnable onSuccess, OnError onError) {

        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede cambiar el estado de las cuentas.");
            return;
        }

        try {
            if (employeeId.equals(SessionManager.getEmployeeId()) && !active) {
                showError(parent, "No puedes desactivar tu propia cuenta de administrador.");
                return;
            }
        } catch (IllegalStateException ignored) {
        }


        String action = active ? "activar" : "desactivar";
        boolean confirmed = showConfirmation(
                parent,
                "¿" + (active ? "Activar" : "Desactivar") + " la cuenta de \"" + userName + "\"?",
                "Confirmar cambio de estado");

        if (!confirmed) return;

        log.info("{} cuenta de empleado: {} (ID={})", action, userName, employeeId);
        runAsyncVoid(
                () -> employeeService.setActive(employeeId, active),
                () -> {
                    log.info("Cuenta {} correctamente: {} (ID={})", action + "da", userName, employeeId);
                    showSuccess(parent, "Cuenta de \"" + userName + "\" " + action + "da correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al {} cuenta {}: {}", action, employeeId, msg);
                    onError.onError("No se pudo " + action + " la cuenta: " + msg, ex);
                }
        );
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    public void create(Employee employee, Component parent, Runnable onSuccess, OnError onError) {
        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede crear empleados.");
            return;
        }
        log.info("Creando empleado para: {} ({})", employee.getFullName(), employee.getEmail());
        runAsyncVoid(
                () -> employeeService.create(employee),
                () -> {
                    log.info("Empleado creado para: {}", employee.getEmail());
                    showSuccess(parent, "Empleado de \"" + employee.getFullName() + "\" creado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al crear empleado {}: {}", employee.getEmail(), msg);
                    onError.onError("No se pudo crear el empleado: " + msg, ex);
                }
        );
    }

    public void update(Employee employee, Component parent, Runnable onSuccess, OnError onError) {
        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede editar empleados.");
            return;
        }
        log.info("Editando empleado para: {} ({})", employee.getFullName(), employee.getEmail());
        runAsyncVoid(
                () -> employeeService.update(employee),
                () -> {
                    log.info("Empleado editado: {}", employee.getEmail());
                    showSuccess(parent, "Empleado editado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al editar empleado {}: {}", employee.getEmail(), msg);
                    onError.onError("No se pudo editar el empleado: " + msg, ex);
                }
        );
    }

    public void delete(String id, Component parent, Runnable onSuccess, OnError onError) {
        if (!SessionManager.isAdmin()) {
            showError(parent, "Solo el administrador puede eliminar empleados.");
            return;
        }
        boolean confirmed = showConfirmation(parent, "¿Está seguro de eliminar este empleado? Esta acción no se puede deshacer.", "Confirmar eliminación");
        if (!confirmed) return;

        log.info("Eliminando empleado ID: {}", id);
        runAsyncVoid(
                () -> employeeService.delete(id),
                () -> {
                    log.info("Empleado eliminado ID: {}", id);
                    showSuccess(parent, "Empleado eliminado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al eliminar empleado ID {}: {}", id, msg);
                    onError.onError("No se pudo eliminar el empleado: " + msg, ex);
                }
        );
    }

    // -------------------------------------------------------
    // Sesión actual (helpers para las vistas)
    // -------------------------------------------------------

    public boolean isAdmin() {
        return SessionManager.isAdmin();
    }

    public String getActiveUserName() {
        try {
            return SessionManager.getFullName();
        } catch (IllegalStateException e) {
            return "";
        }
    }

    public String getActiveEmployeeId() {
        try {
            return SessionManager.getEmployeeId();
        } catch (IllegalStateException e) {
            return "";
        }
    }


    /**
     * Permite al usuario actual (Empleado o Admin) actualizar su propio nombre y contraseña.
     */
    public void updateActiveEmployee(String newFullName, String newPassword,
                                    Component parent, Runnable onSuccess, OnError onError) {
        String employeeId = getActiveEmployeeId();
        if (employeeId.isEmpty()) {
            onError.onError("No hay una sesión activa.", null);
            return;
        }

        log.info("Usuario {} actualizando su propio perfil", employeeId);
        runAsyncVoid(
                () -> {
                    // 1. Actualizar nombre en la tabla employees
                    Employee employee = employeeService.findById(employeeId);
                    employee.setFullName(newFullName);
                    employeeService.update(employee);

                    // 2. Actualizar sesión local
                    SessionManager.updateFullName(newFullName);

                    // 3. Actualizar contraseña si se proporcionó
                    if (newPassword != null && !newPassword.isBlank()) {
                        if (newPassword.length() < 6) {
                            throw new ServiceException("La contraseña debe tener al menos 6 caracteres.");
                        }
                        AuthService authService = new AuthService();
                        try {
                            authService.updateCurrentUser(newPassword);
                        } catch (AuthService.AuthException e) {
                            throw new ServiceException("Error al actualizar contraseña: " + e.getMessage());
                        }
                    }
                },
                () -> {
                    log.info("Perfil actualizado por el propio usuario");
                    showSuccess(parent, "Tu perfil ha sido actualizado correctamente.");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.error("Error al actualizar perfil propio: {}", msg);
                    onError.onError("Error al actualizar perfil: " + msg, ex);
                }
        );
    }
}
