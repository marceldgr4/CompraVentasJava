package com.app.Controllers;

import Infrastructure.security.SessionManager;
import com.app.Model.Enum.RolUser;
import com.app.Service.AuthService;

import javax.swing.*;
import java.awt.Component;

public class AuthController extends BaseController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    public AuthController(){
        this.authService = new AuthService();
    }
    public void login(String email, String password, Component parent,
                      Runnable onSuccess, OnError onError) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            onError.onError("Los campos son obligatorios", null);
            return;
        }

        log.info("Iniciando login : {}", email,password);
                runAsyncVoid(
                        () -> authService.Login(email.trim(), password),
                        () -> {
                            log.info("Login exitoso para: {}", email);
                            onSuccess.run();
                        },
                        (msg, ex) -> {
                            log.warn("Login fallido para {}: {}", email, msg);
                            onError.onError(msg != null ? msg : "Error de autenticación.", ex);
                        }
                );
    }


    public void logout(Component parent, Runnable onSuccess) {
        boolean confirmed = showConfirmation(
                parent, "¿ Seguro que desar cerrar session",
                "Cerrar Sesion");
        if(!confirmed) return;
        log.info("Cerrar session del Usuario: {}", getActiveUserName());
        runAsyncVoid(
                ()-> authService.logout(),
                ()->{
                    log.info("Logout");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.warn("Logout fallido para: {}", msg);
                    onSuccess.run();
                }
        );
    }
    public String getActiveUserName() {
        try {
            return SessionManager.getFullName();
        } catch (IllegalStateException e){
            return "";
        }
    }
/*-------------------------------------------------------
//>>>> REGISTRO DE EMPLEADOS<<<<
 * Registra un nuevo empleado en Supabase Auth.
 * Solo disponible para administradores
 * El trigger {@code on_auth_user_created} en la BD crea automáticamente
 * el registro en la tabla {employees} al confirmar el signup.
    Correo del nuevo empleado (no puede ser nulo ni vacío)
    contraseña (mínimo 6 caracteres)
    nombre completo del empleado (no puede ser nulo ni vacío)
-----------------------------------------------------------------
 */
    public void registerEmployee(String email, String password,
                                 String fullName, RolUser role, Component parent,
                                 Runnable onSuccess, OnError onError) {
        if(!SessionManager.isAdmin()){
            showError(parent,"Solo el administrador puede registrar empleados");
            return;
        }
        if(!validateRegistrationFields(email,password,fullName,parent,onError)){
            return;
        }
        log.info("Iniciando registro : {}",  fullName,email);
        runAsyncVoid(
                ()-> authService.registerEmployee(email.trim(),password,fullName.trim(), role),
                ()->{
                    log.info("Registro exitoso para: {}", email);
                    showSuccess(parent,"Empleado\""+ fullName+ "\"registro exitoso ");
                    onSuccess.run();
                },
                (msg, ex) -> {
                    log.warn("Registro fallido para: {}",email, msg);
                    onError.onError("No se pudo registrar empleados" + msg, ex);
                }
        );
    }

    private boolean validateRegistrationFields(String email, String password,
                                               String fullName, Component parent,
                                               OnError onError) {
        if(fullName == null || fullName.isBlank()) {
            onError.onError("El nombre completo es obligatorio", null);
            return false;
        }
        if(email == null || email.isBlank()||!email.contains("@")||!email.contains(".")) {
            onError.onError("Ingrese un email valido", null);
            return false;
        }
        if(password == null || password.length() < 6) {
            onError.onError("La contraseña debe tener al menos 6 caracteres", null);
            return false;
        }
        return true;
    }

    public boolean IsSessionActive() {
        return SessionManager.isActive();
    }
    public boolean IsAdmin() {
        return SessionManager.isAdmin();
    }

}
