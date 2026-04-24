package com.app.Service;


import Infrastructure.security.SessionManager;
import com.app.Config.AppConfig;
import com.app.Model.Dao.AuthResponse;
import com.app.Model.domain.Profile;
import com.app.Service.exceptions.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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
         

            //Load profile from database to obtain full name and role
            ProfileService profileService = new ProfileService();
            Profile profile = profileService.findById(userId);
            if (!profile.isActive()) {
                throw new AuthException("Usuario deshabilitado. Llame al administrador");
            }

            SessionManager.startSession(
                    userId,
                    profile.getFullName(),
                    profile.getRol(),
                    authResponse.getAccesoToken(),
                    authResponse.getRefreshToken()
            );
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Fallo de red al conectar con Supabase Auth: " + e.getMessage());
        } catch (ServiceException e) {
            throw new AuthException("Fallo al conectar con la base de datos para cargar el perfil: " + e.getMessage());
        }
    }


    // Register the employees(only admin)
    public void registerEmployee(String email, String password, String full_name) throws AuthException {
        if (!SessionManager.isAdmin()) {
            throw new AuthException("Solo el administrador puede registrar empleados");
        }

        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"data\":{\"full_name\":\"%s\",\"rol\":\"Empleado\"}}",
                email, password, full_name
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

    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }
}
