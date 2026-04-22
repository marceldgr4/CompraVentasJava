package com.app.Service;


import Infrastructure.security.SessionManager;
import com.app.Config.AppConfig;
import com.app.Model.Dao.AuthResponse;
import com.app.Model.domain.Profile;
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
        throw new AuthException("The Email is obligatory");
        }
        if(password == null || password.isBlank()) {
            throw new AuthException("The Password is obligatory");
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
                throw new AuthException("Email o password incorrect");
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
                throw new AuthException("User disable. call Admin");
            }

            SessionManager.startSession(
                    userId,
                    profile.getFullName(),
                    profile.getRol(),
                    authResponse.getAccesoToken(),
                    authResponse.getRefreshToken()
            );
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Error the connection" + e.getMessage());
        } catch (ProfileService.ServiceException e) {
            throw new AuthException("Error loading profile" + e.getMessage());
        }
    }


    // Register the employees(only admin)
    public void registerEmployee(String email, String password, String full_name) throws AuthException {
        if (!SessionManager.isAdmin()) {
            throw new AuthException("Only the admin can register employees");
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
                :err.path("message").asText("ERRO Register user");
                throw new AuthException(msg);
            }
        } catch (IOException | InterruptedException e) {
            throw new AuthException("Error the RED" + e.getMessage());
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
