package com.app.Service;


import Infrastructure.security.SessionManager;
import com.app.Config.AppConfig;
import com.app.Model.Dao.AuthResponse;
import com.app.Service.exceptions.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.app.Model.Enum.RolUser;
import com.app.Model.domain.Employee;
import com.app.Service.EmployeeService;

public class AuthService {
    private static final String BASE_URL = AppConfig.get("SUPABASE_URL");
    private static final String ANON_KEY = AppConfig.get("SUPABASE_ANON_KEY");
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private static final ObjectMapper mapper = new ObjectMapper();

    public void Login(String email, String password) throws AuthException {
        if(email == null || email.isBlank()) {
        throw new AuthException("El correo electrónico es obligatorio");
        }
        if(password == null || password.isBlank()) {
            throw new AuthException("La contraseña es obligatoria");
        }

        String body = String.format(
                "{\"email\": \"%s\",\"password\":\"%s\"}", email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/v1/token?grant_type=password"))
                .header("Content-Type", "application/json")
                .header("apikey", ANON_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new AuthException("Correo o contraseña incorrectos");
            }
            AuthResponse authResponse = mapper.readValue(response.body(), AuthResponse.class);
            String userId = authResponse.getUser().getId();
            
            JsonNode json = mapper.readTree(response.body());
            String accessToken = json.get("access_token").asText();
            String refreshToken = json.has("refresh_token") ? json.get("refresh_token").asText() : null;
         

            //Load employee from database to obtain full name and role
            EmployeeService employeeService = new EmployeeService();
            Employee employee;
            try {
                employee = employeeService.findById(userId);
            } catch (ServiceException e) {
                if (e.getMessage().contains("No se encontró el empleado")) {
                    // Auto-repair: Create employee if missing
                    String fullName = authResponse.getUser().getEmail().split("@")[0]; // Fallback name
                    employee = new Employee(userId, authResponse.getUser().getEmail(), fullName, RolUser.Empleado, true);
                    employeeService.create(employee);
                } else {
                    throw e;
                }
            }

            if (!employee.isActive()) {
                throw new AuthException("Usuario deshabilitado. Llame al administrador");
            }

            SessionManager.startSession(
                    userId,
                    employee.getFullName(),
                    employee.getRol(),
                    authResponse.getAccesoToken(),
                    authResponse.getRefreshToken()
            );
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Fallo de red al conectar con Supabase Auth: " + e.getMessage());
        } catch (ServiceException e) {
            throw new AuthException("Fallo al conectar con la base de datos para cargar el empleado: " + e.getMessage());
        }

    }


    // Register the employees(only admin)
    public void registerEmployee(String email, String password, String full_name, RolUser role) throws AuthException {
        if (!SessionManager.isAdmin()) {
            throw new AuthException("Solo el administrador puede registrar empleados");
        }

        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"data\":{\"full_name\":\"%s\",\"rol\":\"%s\"}}",
                email, password, full_name, role.name()
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/v1/signup"))
                .header("Content-Type", "application/json")
                .header("apikey", ANON_KEY)
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 201) {
                JsonNode err = mapper.readTree(response.body());
                String msg = err.has("msg")
                        ? err.get("msg").asText()
                :err.path("message").asText("Error al registrar usuario");
                throw new AuthException(msg);
            }
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Error de red: " + e.getMessage());
        }
    }

    //LOGOUT
    public void logout() {
       if(!SessionManager.isActive()){
       return;
        }
        String token = SessionManager.getAccessToken();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/v1/logout"))
                .header("Authorization", "Bearer " + token)
                .header("apikey", ANON_KEY)
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
        }

        SessionManager.endSession();
    }

    public void updateCurrentUser(String newPassword) throws AuthException {
        if (!SessionManager.isActive()) {
            throw new AuthException("No hay una sesión activa.");
        }

        String token = SessionManager.getAccessToken();
        String body = String.format("{\"password\": \"%s\"}", newPassword);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/v1/user"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .header("apikey", ANON_KEY)
                .method("PUT", HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                JsonNode err = mapper.readTree(response.body());
                String msg = err.has("msg") ? err.get("msg").asText() : "Error al actualizar usuario";
                throw new AuthException(msg);
            }
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Error de red: " + e.getMessage());
        }
    }

    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}
