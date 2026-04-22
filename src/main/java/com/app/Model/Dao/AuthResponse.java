package com.app.Model.Dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {

    @JsonProperty("access_token")
    private String accesoToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("user")
    private UserInfo user;

    public String getAccesoToken() {
        return accesoToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }
    public static class UserInfo {

        @JsonProperty("id")
        private String id;
        @JsonProperty("email")
        private String email;
        @JsonProperty("role")
        private String role;
        @JsonProperty("create_at")
        private String createAt;

        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public String getCreateAt() {
            return createAt;
        }
    }
}
